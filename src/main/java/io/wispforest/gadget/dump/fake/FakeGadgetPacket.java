package io.wispforest.gadget.dump.fake;

import io.wispforest.gadget.dump.read.unwrapped.UnwrappedPacket;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.jetbrains.annotations.Nullable;

/**
 * Marks a fake packet that is not meant to actually be in the network.
 */
public interface FakeGadgetPacket extends Packet<PacketListener> {
    int id();

    void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side);

    default Packet<?> unwrapVanilla() {
        return this;
    }

    default @Nullable UnwrappedPacket unwrapGadget() {
        throw new UnsupportedOperationException("Unrenderable packet.");
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
