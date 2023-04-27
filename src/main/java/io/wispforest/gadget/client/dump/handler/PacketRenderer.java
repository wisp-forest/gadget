package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PacketRenderer {
    Event<PacketRenderer> EVENT = EventFactory.createArrayBacked(PacketRenderer.class, callbacks -> (packet, view, errSink) -> {
        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet.packet())) {
                boolean result = callback.renderPacket(packet, view, errSink);
                if (result)
                    return true;
            } catch (Exception e) {
                errSink.accept(e);
            }
        }

        return false;
    });

    boolean renderPacket(DumpedPacket packet, FlowLayout out, ErrorSink errSink);
}