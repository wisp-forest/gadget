package io.wispforest.gadget.dump.write;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

public class PacketDumpWriter implements AutoCloseable {
    public static final int VERSION = 1;

    protected @Nullable OutputStream output;
    protected final Path path;

    public PacketDumpWriter(Path path) throws IOException {
        this.path = path;
        this.output = new GZIPOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE), true);

        ByteBuf headerBuf = Unpooled.buffer(15);
        headerBuf.writeBytes("gadget:dump".getBytes(StandardCharsets.UTF_8));
        headerBuf.writeInt(VERSION);
        headerBuf.getBytes(headerBuf.readerIndex(), output, headerBuf.readableBytes());
    }

    public Path path() {
        return path;
    }

    public void write(Packet<?> packet, NetworkState state, NetworkSide side) {
        if (output == null) return;

        PacketByteBuf buf = PacketByteBufs.create();

        short flags = 0;

        if (side == NetworkSide.SERVERBOUND)
            flags |= 0b00000001;

        switch (state) {
            case HANDSHAKING -> { }
            case PLAY -> flags |= 0b0010;
            case STATUS -> flags |= 0b0100;
            case LOGIN -> flags |= 0b0110;
        }

        try (var ignored = NetworkUtil.writeByteLength(buf)) {
            buf.writeShort(flags);

            buf.writeLong(System.currentTimeMillis());

            PacketDumping.writePacket(buf, packet, state, side);
        }

        synchronized (this) {
            var out = output;

            if (out == null) return;

            try {
                buf.getBytes(buf.readerIndex(), out, buf.readableBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isClosed() {
        return output == null;
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            if (output == null) return;

            output.close();
            output = null;
        }
    }
}
