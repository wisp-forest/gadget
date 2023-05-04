package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.client.dump.SearchWord;
import io.wispforest.gadget.dump.fake.GadgetReadErrorPacket;
import io.wispforest.gadget.dump.fake.GadgetWriteErrorPacket;
import io.wispforest.gadget.dump.read.handler.PlainTextPacketDumper;
import io.wispforest.gadget.util.*;
import net.minecraft.client.resource.language.I18n;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public class PacketDumpReader {
    private final List<DumpedPacket> packets;
    private final long startTime;
    private final long endTime;

    public PacketDumpReader(Path path, ProgressToast toast) throws IOException {
        try (var is = toast.loadWithProgress(path)) {
            if (path.toString().endsWith(".dump"))
                this.packets = PacketDumpDeserializer.readV0(is);
            else
                this.packets = PacketDumpDeserializer.readNew(is);
        }

        if (packets.size() > 0) {
            startTime = packets.get(0).sentAt();
            endTime = packets.get(packets.size() - 1).sentAt();
        } else {
            startTime = endTime = 0;
        }
    }

    public List<DumpedPacket> packets() {
        return packets;
    }

    public List<DumpedPacket> collectFor(String searchText, long from, int max) {
        return collectFor(searchText, from, max, unused -> {}, CancellationToken.NONE);
    }

    public List<DumpedPacket> collectFor(String searchText, long from, int max, IntConsumer progressConsumer, CancellationToken token) {
        List<SearchWord> words = SearchWord.parseSearch(searchText);
        List<DumpedPacket> collected = new ArrayList<>();

        token.throwIfCancelled();

        outer:
        for (var packet : packets) {
            if (packet.sentAt() < from) continue;
            if (packet.sentAt() > from && collected.size() > max) break;

            String relevantText = packet.get(SearchTextData.KEY).searchText();

            token.throwIfCancelled();

            for (var word : words) {
                if (!word.matches(relevantText))
                    continue outer;
            }

            collected.add(packet);
            progressConsumer.accept(collected.size());

            token.throwIfCancelled();
        }

        token.throwIfCancelled();
        return collected;
    }

    public void dumpPacketToText(DumpedPacket packet, FormattedDumper out, int indent) {
        StringBuilder sb = new StringBuilder();

        if (packet.packet() instanceof GadgetReadErrorPacket errorPacket) {
            sb.append(I18n.translate("text.gadget.packet_read_error", errorPacket.packetId()));
        } else if (packet.packet() instanceof GadgetWriteErrorPacket errorPacket) {
            sb.append(I18n.translate("text.gadget.packet_write_error", errorPacket.packetId()));
        } else {
            sb.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

            if (packet.channelId() != null)
                sb.append(" ").append(packet.channelId());
        }

        if (startTime < endTime) {
            sb.append(" [");
            sb.append(DurationFormatUtils.formatDurationHMS(packet.sentAt() - startTime));
            sb.append("]");
        }

        out.write(indent, sb.toString());

        if (packet.packet() instanceof GadgetReadErrorPacket error) {
            out.writeLines(indent + 1, ThrowableUtil.throwableToString(error.exception()));
        }

        if (packet.packet() instanceof GadgetWriteErrorPacket error) {
            out.writeLines(indent + 1, error.exceptionText());
        }

        PlainTextPacketDumper.EVENT.invoker().dumpAsPlainText(packet, out, indent + 1, throwable -> {
            out.write(indent + 1, "----ERROR----");
            out.writeLines(indent + 1, ThrowableUtil.throwableToString(throwable));
            out.write(indent + 1, "-------------");
        });
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }
}
