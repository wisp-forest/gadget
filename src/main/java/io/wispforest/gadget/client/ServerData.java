package io.wispforest.gadget.client;

import io.wispforest.gadget.network.GadgetNetworking;
import io.wispforest.gadget.network.packet.s2c.AnnounceS2CPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.Nullable;

public class ServerData {
    private static @Nullable AnnounceS2CPacket ANNOUNCE_PACKET;

    public static void init() {
        GadgetNetworking.CHANNEL.registerClientbound(AnnounceS2CPacket.class,
            (message, access) -> ANNOUNCE_PACKET = message);

        ClientPlayConnectionEvents.DISCONNECT.register(
            (handler, client) -> ANNOUNCE_PACKET = null);

        ClientLoginConnectionEvents.DISCONNECT.register(
            (handler, client) -> ANNOUNCE_PACKET = null);
    }

    public static boolean canReplaceStacks() {
        return ANNOUNCE_PACKET != null && ANNOUNCE_PACKET.canReplaceStacks();
    }

    public static boolean canRequestServerData() {
        return ANNOUNCE_PACKET != null && ANNOUNCE_PACKET.canRequestServerData();
    }
}
