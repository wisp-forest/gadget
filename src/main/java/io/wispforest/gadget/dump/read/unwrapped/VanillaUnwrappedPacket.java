package io.wispforest.gadget.dump.read.unwrapped;

import net.minecraft.network.packet.Packet;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public record VanillaUnwrappedPacket(Packet<?> packet) implements FieldsUnwrappedPacket {
    @Override
    public @Nullable Text headText() {
        return null;
    }

    @Override
    public @Nullable Object rawFieldsObject() {
        return packet;
    }
}
