package io.wispforest.gadget.testmod.client;

import io.wispforest.gadget.Gadget;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public record EpicPacket(String maldenhagen) implements FabricPacket {
    public static final PacketType<EpicPacket> TYPE = PacketType.create(Gadget.id("epic"), EpicPacket::read);

    public static EpicPacket read(PacketByteBuf buf) {
        return new EpicPacket(buf.readString());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeString(maldenhagen);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
