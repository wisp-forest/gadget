package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.dump.read.UnwrappedPacketData;

public class ClientPacketHandlers {
    private ClientPacketHandlers() {

    }

    public static void init() {
        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            var unwrapped = packet.get(UnwrappedPacketData.KEY).unwrap();

            if (unwrapped == null) return;

            unwrapped.render(view, errSink);
        });
    }
}
