package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.FormattedDumper;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PlainTextPacketDumper {
    Event<PlainTextPacketDumper> EVENT = EventFactory.createArrayBacked(PlainTextPacketDumper.class, callbacks -> (packet, out, indent, errSink) -> {
        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet.packet())) {
                if (callback.dumpAsPlainText(packet, out, indent, errSink))
                    return true;
            } catch (Exception e) {
                errSink.accept(e);
            }
        }

        return false;
    });

    boolean dumpAsPlainText(DumpedPacket packet, FormattedDumper out, int indent, ErrorSink errSink);
}
