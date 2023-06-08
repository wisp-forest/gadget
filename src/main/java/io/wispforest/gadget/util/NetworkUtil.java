package io.wispforest.gadget.util;

import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public final class NetworkUtil {
    private static final ThreadLocal<Supplier<PacketByteBuf>> TMP_BUF_SOURCE = ThreadLocal.withInitial(() -> SupplierUtil.weakLazy(PacketByteBufs::create));

    private NetworkUtil() {

    }

    public static Identifier getChannelOrNull(Packet<?> packet) {
        if (packet instanceof CustomPayloadS2CPacket pkt)
            return pkt.getChannel();
        else if (packet instanceof CustomPayloadC2SPacket pkt)
            return pkt.getChannel();
        else if (packet instanceof LoginQueryRequestS2CPacket pkt)
            return pkt.getChannel();
        else
            return null;
    }

    public static PacketByteBuf unwrapCustom(Packet<?> packet) {
        if (packet instanceof CustomPayloadS2CPacket pkt)
            return pkt.getData();
        else if (packet instanceof CustomPayloadC2SPacket pkt)
            return pkt.getData();
        else if (packet instanceof LoginQueryRequestS2CPacket pkt)
            return pkt.getPayload();
        else if (packet instanceof LoginQueryResponseC2SPacket pkt)
            return pkt.getResponse();
        else return null;
    }

    public static InfallibleClosable resetIndexes(Packet<?> packet) {
        PacketByteBuf buf = unwrapCustom(packet);

        if (buf == null) {
            return () -> { };
        } else {
            return resetIndexes(buf);
        }
    }

    public static InfallibleClosable resetIndexes(ByteBuf buf) {
        int readerIdx = buf.readerIndex();
        int writerIdx = buf.writerIndex();

        return () -> {
            buf.readerIndex(readerIdx);
            buf.writerIndex(writerIdx);
        };
    }

    public static InfallibleClosable writeByteLength(PacketByteBuf buf) {
        int idIdx = buf.writerIndex();
        buf.writeInt(0);
        int startIdx = buf.writerIndex();

        return () -> {
            int endIdx = buf.writerIndex();
            buf.writerIndex(idIdx);
            buf.writeInt(endIdx - startIdx);
            buf.writerIndex(endIdx);
        };
    }

    public static PacketByteBuf readOfLengthIntoTmp(PacketByteBuf buf) {
        PacketByteBuf tmpBuf = TMP_BUF_SOURCE.get().get();
        int length = buf.readInt();

        tmpBuf.readerIndex(0);
        tmpBuf.writerIndex(0);
        tmpBuf.writeBytes(buf, length);

        return tmpBuf;
    }
}
