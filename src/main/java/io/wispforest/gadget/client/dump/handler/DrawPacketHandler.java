package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.dump.DumpedPacket;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.owo.ui.container.FlowLayout;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface DrawPacketHandler {
    Event<DrawPacketHandler> EVENT = EventFactory.createArrayBacked(DrawPacketHandler.class, callbacks -> (packet, view) -> {
        packet.drawErrors().clear();

        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet.packet())) {
                boolean result = callback.onDrawPacket(packet, view);
                if (result)
                    return true;
            } catch (Exception e) {
                packet.drawErrors().add(e);
            }
        }

        return false;
    });

    boolean onDrawPacket(DumpedPacket packet, FlowLayout view);
}