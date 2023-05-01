package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.dump.fake.GadgetReadErrorPacket;
import io.wispforest.gadget.dump.fake.GadgetWriteErrorPacket;
import io.wispforest.gadget.field.DefaultFieldDataHolder;
import io.wispforest.gadget.field.FieldDataHolder;
import io.wispforest.gadget.field.FieldDataNode;
import io.wispforest.gadget.field.LocalFieldDataSource;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.util.ReflectionUtil;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

public final class PacketHandlers {
    public static final Identifier FIRST_PHASE = Gadget.id("first");
    public static final Identifier LAST_PHASE = Gadget.id("last");


    private PacketHandlers() {

    }

    public static void init() {
        OwoSupport.init();
        FapiSupport.init();

        SearchTextGatherer.EVENT.addPhaseOrdering(FIRST_PHASE, Event.DEFAULT_PHASE);
        SearchTextGatherer.EVENT.register(FIRST_PHASE, (packet, searchText, errSink) -> {
            searchText.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

            if (packet.channelId() != null)
                searchText.append(" ").append(packet.channelId());
        });

        SearchTextGatherer.EVENT.register((packet, out, errSink) -> {
            var unwrapped = PacketUnwrapper.EVENT.invoker().tryUnwrap(packet, errSink);

            if (unwrapped == null) return;

            out.append(" ").append(ReflectionUtil.nameWithoutPackage(unwrapped.packet().getClass()));
        });

        PlainTextPacketDumper.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST_PHASE);
        PlainTextPacketDumper.EVENT.register(LAST_PHASE, (packet, out, indent, errSink) -> {
            if (packet.packet() instanceof GadgetReadErrorPacket errorPacket) {
                out.writeHexDump(indent, errorPacket.data());
                return true;
            }

            if (packet.packet() instanceof GadgetWriteErrorPacket) return true;

            if (packet.channelId() != null) {
                var buf = NetworkUtil.unwrapCustom(packet.packet());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                out.writeHexDump(indent, bytes);
                return true;
            }

            DefaultFieldDataHolder holder = new DefaultFieldDataHolder(
                new LocalFieldDataSource(packet.packet(), false),
                true
            );

            holder.dumpToText(out, indent, holder.root(), 5)
                .join();

            return true;
        });
    }
}
