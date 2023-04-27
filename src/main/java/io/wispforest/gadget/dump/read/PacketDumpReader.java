package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.client.dump.SearchWord;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.core.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        List<SearchWord> words = SearchWord.parseSearch(searchText);
        List<DumpedPacket> collected = new ArrayList<>();

        outer:
        for (var packet : packets) {
            if (packet.sentAt() < from) continue;
            if (packet.sentAt() > from && collected.size() > max) break;

            String relevantText = packet.get(SearchTextData.KEY).searchText();

            for (var word : words) {
                if (!word.matches(relevantText))
                    continue outer;
            }

            collected.add(packet);
        }

        return collected;
    }

    public long startTime() {
        return startTime;
    }

    public long endTime() {
        return endTime;
    }
}
