package io.wispforest.gadget.dump;

import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;

/**
 * Marks a fake packet that is not meant to actually be in the network.
 */
public interface FakeGadgetPacket extends Packet<PacketListener> {
    int id();

    void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side);

    default Packet<?> unwrap() {
        return this;
    }

    @Override
    default void write(PacketByteBuf buf) {
        throw new IllegalStateException();
    }

    @Override
    default void apply(PacketListener listener) {
        throw new IllegalStateException();
    }

    @FunctionalInterface
    interface Reader<T extends FakeGadgetPacket> {
        T read(PacketByteBuf buf, NetworkState state, NetworkSide side);
    }
}
