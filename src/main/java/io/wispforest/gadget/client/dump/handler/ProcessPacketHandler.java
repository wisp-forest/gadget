package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.dump.DumpedPacket;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface ProcessPacketHandler {
    Event<ProcessPacketHandler> EVENT = EventFactory.createArrayBacked(ProcessPacketHandler.class, callbacks -> (packet, view, searchText) -> {
        for (var callback : callbacks) {
            boolean result = callback.onProcessPacket(packet, view, searchText);

            if (result)
                return true;
        }

        return false;
    });

    boolean onProcessPacket(DumpedPacket packet, VerticalFlowLayout view, StringBuilder searchText);
}
