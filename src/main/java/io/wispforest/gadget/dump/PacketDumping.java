package io.wispforest.gadget.dump;

import io.wispforest.gadget.Gadget;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;

public final class PacketDumping {
    private PacketDumping() {

    }

    public static void writePacket(PacketByteBuf buf, Packet<?> packet, NetworkState state, NetworkSide side) {
        int startWriteIdx = buf.writerIndex();
        int packetId = 0;

        try {
            if (packet instanceof BundlePacket<?> bundle) {
                packet = GadgetBundlePacket.wrap(bundle);
            }

            if (packet instanceof FakeGadgetPacket fakePacket) {
                packetId = fakePacket.id();
                buf.writeVarInt(packetId);
                fakePacket.writeToDump(buf, state, side);
                return;
            }

            packetId = state.getPacketId(side, packet);

            if (packetId == -1)
                throw new UnsupportedOperationException(packet.getClass().getName() + " is an invalid packet in " + side + " " + state);

            buf.writeVarInt(packetId);

            packet.write(buf);
        } catch (Exception e) {
            buf.writerIndex(startWriteIdx);

            Gadget.LOGGER.error("Error while writing packet {}", packet, e);

            GadgetWriteErrorPacket writeError = GadgetWriteErrorPacket.fromThrowable(packetId, e);
            buf.writeVarInt(writeError.id());
            writeError.writeToDump(buf, state, side);
        }
    }

    public static Packet<?> readPacket(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        int startOfData = buf.readerIndex();
        int packetId = buf.readVarInt();

        try {
            switch (packetId) {
                case GadgetWriteErrorPacket.ID -> {
                    return GadgetWriteErrorPacket.read(buf);
                }
                case GadgetBundlePacket.ID -> {
                    return GadgetBundlePacket.read(buf, state, side).unwrap();
                }
                default -> { }
            }

            return state.getPacketHandler(side, packetId, buf);
        } catch (Exception e) {
            buf.readerIndex(startOfData);
            return GadgetReadErrorPacket.from(buf, packetId, e);
        }
    }
}
