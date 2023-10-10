package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.dump.write.PacketDumping;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.util.ProgressToast;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.zip.GZIPInputStream;

public class PacketDumpDeserializer {
    private PacketDumpDeserializer() {

    }

    public static ReadPacketDump readFrom(ProgressToast toast, Path path) throws IOException {
        try (var is = toast.loadWithProgress(path)) {
            if (path.toString().endsWith(".dump"))
                return PacketDumpDeserializer.readV0(is);
            else
                return PacketDumpDeserializer.readNew(is);
        }
    }

    public static ReadPacketDump readV0(InputStream is) {
        List<DumpedPacket> list = new ArrayList<>();

        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            PacketByteBuf buf = PacketByteBufs.create();

            Int2ObjectMap<Identifier> loginQueryChannels = new Int2ObjectOpenHashMap<>();

            while (true) {
                OptionalInt length = readInt(bis, true);

                if (length.isEmpty()) return new ReadPacketDump(list, null);

                buf.readerIndex(0);
                buf.writerIndex(0);

                buf.writeBytes(bis.readNBytes(length.getAsInt()));

                short flags = buf.readShort();
                boolean outbound = (flags & 1) != 0;
                NetworkState state = switch (flags & 0b1110) {
                    case 0b0000 -> NetworkState.HANDSHAKING;
                    case 0b0100 -> NetworkState.STATUS;
                    case 0b0110 -> NetworkState.LOGIN;
                    case 0b1110 -> NetworkState.CONFIGURATION;
                    case 0b0010 -> NetworkState.PLAY;
                    default -> throw new IllegalStateException();
                };
                int size = buf.readableBytes();
                Packet<?> packet = PacketDumping.readPacket(buf, state,  outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND);
                Identifier channelId = NetworkUtil.getChannelOrNull(packet);

                if (packet instanceof LoginQueryRequestS2CPacket req) {
                    loginQueryChannels.put(req.queryId(), req.payload().id());
                } else if (packet instanceof LoginQueryResponseC2SPacket res) {
                    channelId = loginQueryChannels.get(res.queryId());
                }

                list.add(new DumpedPacket(outbound, state, packet, channelId, 0, size));
            }
        } catch (IOException e) {
            return new ReadPacketDump(list, e);
        }
    }

    public static ReadPacketDump readNew(InputStream is) throws IOException {
        try (BufferedInputStream dis = new BufferedInputStream(new GZIPInputStream(is))) {
            var magic = dis.readNBytes(11);

            if (!Arrays.equals(magic, "gadget:dump".getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalStateException("Invalid gdump file!");
            }

            var version = readInt(dis, false).orElseThrow();

            if (version == 1)
                return readV1(dis);
            else
                throw new IllegalStateException("Invalid gdump version " + version);
        }
    }

    private static ReadPacketDump readV1(InputStream is) {
        List<DumpedPacket> list = new ArrayList<>();

        PacketByteBuf buf = PacketByteBufs.create();

        Int2ObjectMap<Identifier> loginQueryChannels = new Int2ObjectOpenHashMap<>();

        try {
            while (true) {
                OptionalInt len = readInt(is, true);

                if (len.isEmpty())
                    return new ReadPacketDump(list, null);

                buf.readerIndex(0);
                buf.writerIndex(0);

                buf.writeBytes(is.readNBytes(len.getAsInt()));

                short flags = buf.readShort();
                boolean outbound = (flags & 1) != 0;
                NetworkState state = switch (flags & 0b1110) {
                    case 0b0000 -> NetworkState.HANDSHAKING;
                    case 0b0100 -> NetworkState.STATUS;
                    case 0b0110 -> NetworkState.LOGIN;
                    case 0b1110 -> NetworkState.CONFIGURATION;
                    case 0b0010 -> NetworkState.PLAY;
                    default -> throw new IllegalStateException();
                };
                long sentAt = buf.readLong();
                int size = buf.readableBytes();
                Packet<?> packet = PacketDumping.readPacket(buf, state, outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND);
                Identifier channelId = NetworkUtil.getChannelOrNull(packet);

                if (packet instanceof LoginQueryRequestS2CPacket req) {
                    loginQueryChannels.put(req.queryId(), req.payload().id());
                } else if (packet instanceof LoginQueryResponseC2SPacket res) {
                    channelId = loginQueryChannels.get(res.queryId());
                }

                list.add(new DumpedPacket(outbound, state, packet, channelId, sentAt, size));
            }
        } catch (IOException e) {
            return new ReadPacketDump(list, e);
        }
    }

    // I hate DataInputStream.
    private static OptionalInt readInt(InputStream is, boolean gracefulEof) throws IOException {
        int ch1 = is.read();
        int ch2 = is.read();
        int ch3 = is.read();
        int ch4 = is.read();

        if (gracefulEof && ch1 < 0)
            return OptionalInt.empty();
        else if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();

        return OptionalInt.of(((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + ch4));
    }

    public record ReadPacketDump(List<DumpedPacket> packets, @Nullable IOException finalError) {

    }
}
