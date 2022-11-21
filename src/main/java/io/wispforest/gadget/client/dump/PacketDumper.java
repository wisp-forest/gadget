package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.NotificationToast;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

public class PacketDumper {
    public static final int VERSION = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger("gadget/PacketDumper");
    public static final Path DUMP_DIR = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("dumps");

    private static Path DUMP_PATH;
    private static WritableByteChannel OUTPUT_CHANNEL;

    private PacketDumper() {

    }

    public static void start(boolean doToast) {
        try {
            if (!Files.exists(DUMP_DIR))
                Files.createDirectories(DUMP_DIR);

            String filename = Util.getFormattedCurrentTime() + ".gdump";
            DUMP_PATH = DUMP_DIR.resolve(filename);
            OUTPUT_CHANNEL =
                Channels.newChannel(
                    new GZIPOutputStream(Files.newOutputStream(DUMP_PATH, StandardOpenOption.WRITE, StandardOpenOption.CREATE))
                );
            ByteBuffer headerBuf = ByteBuffer.allocate(15);
            headerBuf.put("gadget:dump".getBytes(StandardCharsets.UTF_8));
            headerBuf.putInt(VERSION);
            headerBuf.flip();
            OUTPUT_CHANNEL.write(headerBuf);

            LOGGER.info("Started dumping to {}", filename);

            if (doToast)
                MinecraftClient.getInstance().getToastManager().add(new NotificationToast(Text.translatable("message.gadget.dump.started"), null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void stop() {
        try {
            LOGGER.info("Saved dump to {}", DUMP_PATH);
            MinecraftClient.getInstance().getToastManager().add(new NotificationToast(Text.translatable("message.gadget.dump.stopped"), Text.literal(DUMP_PATH.getFileName().toString())));

            OUTPUT_CHANNEL.close();

            OUTPUT_CHANNEL = null;
            DUMP_PATH = null;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void dump(boolean outbound, Packet<?> packet) {
        if (packet instanceof ChunkDataS2CPacket && Gadget.CONFIG.dropChunkData())
            return;

        PacketByteBuf buf = PacketByteBufs.create();
        NetworkState state = NetworkState.getPacketHandlerState(packet);

        short flags = 0;

        if (outbound)
            flags |= 0b00000001;

        switch (state) {
            case HANDSHAKING -> { }
            case PLAY -> flags |= 0b0010;
            case STATUS -> flags |= 0b0100;
            case LOGIN -> flags |= 0b0110;
        }

        buf.writeInt(0);
        buf.writeShort(flags);

        buf.writeLong(System.currentTimeMillis());

        Integer packetId = state.getPacketId(outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND, packet);

        if (packetId == null)
            throw new UnsupportedOperationException("Invalid packet: " + packet);

        buf.writeVarInt(packetId);

        packet.write(buf);

        int totalLength = buf.readableBytes();
        int prevWriterIdx = buf.writerIndex();

        buf.writerIndex(0);
        buf.writeInt(totalLength - 4);
        buf.writerIndex(prevWriterIdx);

        ByteBuffer nioBuf = buf.nioBuffer();

        try {
            OUTPUT_CHANNEL.write(nioBuf);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isDumping() {
        return OUTPUT_CHANNEL != null;
    }
}
