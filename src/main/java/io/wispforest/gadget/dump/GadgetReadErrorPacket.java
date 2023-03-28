package io.wispforest.gadget.dump;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;

public record GadgetReadErrorPacket(byte[] data, int packetId, Exception exception) implements FakeGadgetPacket {
    public static final int ID = -3;

    public static GadgetReadErrorPacket from(PacketByteBuf buf, int packetId, Exception exception) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return new GadgetReadErrorPacket(data, packetId, exception);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        throw new UnsupportedOperationException();
    }
}
