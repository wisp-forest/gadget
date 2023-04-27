package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.util.ContextData;
import net.minecraft.network.NetworkState;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

public final class DumpedPacket extends ContextData<DumpedPacket> {
    private final boolean outbound;
    private final NetworkState state;
    private final Packet<?> packet;
    private final Identifier channelId;
    private final long sentAt;
    private final int size;

    public DumpedPacket(boolean outbound, NetworkState state, Packet<?> packet, Identifier channelId, long sentAt,
                        int size) {
        this.outbound = outbound;
        this.state = state;
        this.packet = packet;
        this.channelId = channelId;
        this.sentAt = sentAt;
        this.size = size;
    }

    public int color() {
        return switch (state) {
            case PLAY -> 0xFF00FF00;
            case HANDSHAKING -> 0xFF808080;
            case LOGIN -> 0xFFFF0000;
            case STATUS -> 0xFFFFFF00;
        };
    }

    public boolean outbound() {
        return outbound;
    }

    public NetworkState state() {
        return state;
    }

    public Packet<?> packet() {
        return packet;
    }

    public Identifier channelId() {
        return channelId;
    }

    public long sentAt() {
        return sentAt;
    }

    public int size() {
        return size;
    }
}
