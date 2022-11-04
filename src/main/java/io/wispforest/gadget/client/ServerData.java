package io.wispforest.gadget.client;

import io.wispforest.gadget.network.AnnounceS2CPacket;
import io.wispforest.gadget.network.GadgetNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ServerData {
    public static AnnounceS2CPacket ANNOUNCE_PACKET;

    public static void init() {
        GadgetNetworking.CHANNEL.registerClientbound(AnnounceS2CPacket.class,
            (message, access) -> ANNOUNCE_PACKET = message);

        ClientPlayConnectionEvents.DISCONNECT.register(
            (handler, client) -> ANNOUNCE_PACKET = null);

        ClientLoginConnectionEvents.DISCONNECT.register(
            (handler, client) -> ANNOUNCE_PACKET = null);
    }
}
