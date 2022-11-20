package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.dump.DumpedPacket;
import io.wispforest.gadget.util.WrappingEvent;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import net.fabricmc.fabric.api.event.Event;

@Deprecated(forRemoval = true)
public interface DrawPacketHandler {
    Event<DrawPacketHandler> EVENT = new WrappingEvent<>(ProcessPacketHandler.EVENT,
        h -> (packet, view, sb) -> h.onDrawPacket(packet, view));

    boolean onDrawPacket(DumpedPacket packet, VerticalFlowLayout view);
}