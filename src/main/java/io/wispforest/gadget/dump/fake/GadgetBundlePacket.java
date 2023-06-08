package io.wispforest.gadget.dump.fake;

import com.google.common.collect.Lists;
import io.wispforest.gadget.dump.write.PacketDumping;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;

import java.util.ArrayList;
import java.util.List;

public record GadgetBundlePacket(NetworkState state, NetworkSide side, List<Packet<?>> packets) implements FakeGadgetPacket {
    public static final int ID = -2;

    public static GadgetBundlePacket wrap(BundlePacket<?> bundle) {
        List<Packet<?>> packets = Lists.newArrayList(bundle.getPackets());

        if (bundle instanceof BundleS2CPacket) {
            return new GadgetBundlePacket(NetworkState.PLAY, NetworkSide.CLIENTBOUND, packets);
        } else {
            throw new IllegalStateException("Unknown bundle packet type " + bundle);
        }
    }

    public static GadgetBundlePacket read(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        int size = buf.readVarInt();
        List<Packet<?>> packets = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            PacketByteBuf subBuf = NetworkUtil.readOfLengthIntoTmp(buf);
            packets.add(PacketDumping.readPacket(subBuf, state, side));
        }

        return new GadgetBundlePacket(state, side, packets);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        buf.writeVarInt(packets.size());

        for (var subPacket : packets) {
            try (var ignored = NetworkUtil.writeByteLength(buf)) {
                PacketDumping.writePacket(buf, subPacket, state, side);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Packet<?> unwrapVanilla() {
        if (state == NetworkState.PLAY && side == NetworkSide.CLIENTBOUND) {
            // java i promise this cast is fine
            return new BundleS2CPacket((Iterable<Packet<ClientPlayPacketListener>>)(Object) packets);
        } else {
            throw new IllegalStateException("No such BundlePacket type for " + state + " and " + side);
        }
    }
}
