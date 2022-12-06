package io.wispforest.gadget.client.dump;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;

public class ErrorPacket implements Packet<PacketListener> {
    private final byte[] data;
    private final int packetId;
    private final Exception exception;

    public ErrorPacket(PacketByteBuf buf, int packetId, Exception exception) {
        this.data = new byte[buf.readableBytes()];
        this.packetId = packetId;
        this.exception = exception;
        buf.readBytes(this.data);
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBytes(data);
    }

    public byte[] getData() {
        return data;
    }

    public int getPacketId() {
        return packetId;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public void apply(PacketListener listener) {
        throw new UnsupportedOperationException("bruh");
    }
}
