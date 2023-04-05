package io.wispforest.gadget.dump;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public record GadgetWriteErrorPacket(int packetId, String exceptionText) implements FakeGadgetPacket {
    public static final int ID = -1;

    public static GadgetWriteErrorPacket fromThrowable(int packetId, Throwable t) {
        CharArrayWriter writer = new CharArrayWriter();
        t.printStackTrace(new PrintWriter(writer));
        String fullExceptionText = writer.toString();
        return new GadgetWriteErrorPacket(packetId, fullExceptionText);
    }

    public static GadgetWriteErrorPacket read(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        return new GadgetWriteErrorPacket(buf.readVarInt(), buf.readString());
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        buf.writeVarInt(packetId);
        buf.writeString(exceptionText);
    }
}
