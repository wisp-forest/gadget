package me.basiqueevangelist.gadget.client.dump;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;

public record DumpedPacket(boolean outbound, NetworkState state, Packet<?> packet) {
    public static DumpedPacket read(PacketByteBuf buf) {
        short flags = buf.readShort();
        boolean outbound = (flags & 1) != 0;
        NetworkState state = switch (flags & 0b0110) {
            case 0b0000 -> NetworkState.HANDSHAKING;
            case 0b0010 -> NetworkState.PLAY;
            case 0b0100 -> NetworkState.STATUS;
            case 0b0110 -> NetworkState.LOGIN;
            default -> throw new IllegalStateException();
        };
        int packetId = buf.readVarInt();
        Packet<?> packet = state.getPacketHandler(outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND, packetId, buf);

        return new DumpedPacket(outbound, state, packet);
    }

    public int color() {
        return switch (state) {
            case PLAY -> 0xFF00FF00;
            case HANDSHAKING -> 0xFF808080;
            case LOGIN -> 0xFFFF0000;
            case STATUS -> 0xFFFFFF00;
        };
    }
}
