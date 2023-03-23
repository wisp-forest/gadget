package io.wispforest.gadget.client.dump;

import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import java.util.List;

public record DumpedPacket(boolean outbound, NetworkState state, Packet<?> packet, Identifier channelId, long sentAt,
                           int size, List<Exception> searchTextErrors, List<Exception> drawErrors) {
    public int color() {
        return switch (state) {
            case PLAY -> 0xFF00FF00;
            case HANDSHAKING -> 0xFF808080;
            case LOGIN -> 0xFFFF0000;
            case STATUS -> 0xFFFFFF00;
        };
    }
}
