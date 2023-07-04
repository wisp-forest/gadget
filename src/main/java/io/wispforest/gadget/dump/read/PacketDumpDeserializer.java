package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.dump.write.PacketDumping;
import io.wispforest.gadget.util.NetworkUtil;
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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.zip.GZIPInputStream;

public class PacketDumpDeserializer {
    private PacketDumpDeserializer() {

    }

    public static List<DumpedPacket> readV0(InputStream is) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(is)) {
            List<DumpedPacket> list = new ArrayList<>();
            PacketByteBuf buf = PacketByteBufs.create();

            Int2ObjectMap<Identifier> loginQueryChannels = new Int2ObjectOpenHashMap<>();

            while (true) {
                OptionalInt length = readInt(bis, true);

                if (length.isEmpty()) return list;

                buf.readerIndex(0);
                buf.writerIndex(0);

                buf.writeBytes(bis.readNBytes(length.getAsInt()));

                short flags = buf.readShort();
                boolean outbound = (flags & 1) != 0;
                NetworkState state = switch (flags & 0b0110) {
                    case 0b0000 -> NetworkState.HANDSHAKING;
                    case 0b0010 -> NetworkState.PLAY;
                    case 0b0100 -> NetworkState.STATUS;
                    case 0b0110 -> NetworkState.LOGIN;
                    default -> throw new IllegalStateException();
                };
                int size = buf.readableBytes();
                Packet<?> packet = PacketDumping.readPacket(buf, state,  outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND);
                Identifier channelId = NetworkUtil.getChannelOrNull(packet);

                if (packet instanceof LoginQueryRequestS2CPacket req) {
                    loginQueryChannels.put(req.getQueryId(), req.getChannel());
                } else if (packet instanceof LoginQueryResponseC2SPacket res) {
                    channelId = loginQueryChannels.get(res.getQueryId());
                }

                list.add(new DumpedPacket(outbound, state, packet, channelId, 0, size));
            }
        }
    }

    public static List<DumpedPacket> readNew(InputStream is) throws IOException {
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

    private static List<DumpedPacket> readV1(InputStream is) throws IOException {
        List<DumpedPacket> list = new ArrayList<>();

        PacketByteBuf buf = PacketByteBufs.create();

        Int2ObjectMap<Identifier> loginQueryChannels = new Int2ObjectOpenHashMap<>();

        while (true) {
            OptionalInt len = readInt(is, true);

            if (len.isEmpty())
                return list;

            buf.readerIndex(0);
            buf.writerIndex(0);

            buf.writeBytes(is.readNBytes(len.getAsInt()));

            short flags = buf.readShort();
            boolean outbound = (flags & 1) != 0;
            NetworkState state = switch (flags & 0b0110) {
                case 0b0000 -> NetworkState.HANDSHAKING;
                case 0b0010 -> NetworkState.PLAY;
                case 0b0100 -> NetworkState.STATUS;
                case 0b0110 -> NetworkState.LOGIN;
                default -> throw new IllegalStateException();
            };
            long sentAt = buf.readLong();
            int size = buf.readableBytes();
            Packet<?> packet = PacketDumping.readPacket(buf, state, outbound ? NetworkSide.SERVERBOUND : NetworkSide.CLIENTBOUND);
            Identifier channelId = NetworkUtil.getChannelOrNull(packet);

            if (packet instanceof LoginQueryRequestS2CPacket req) {
                loginQueryChannels.put(req.getQueryId(), req.getChannel());
            } else if (packet instanceof LoginQueryResponseC2SPacket res) {
                channelId = loginQueryChannels.get(res.getQueryId());
            }

            list.add(new DumpedPacket(outbound, state, packet, channelId, sentAt, size));
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
}
