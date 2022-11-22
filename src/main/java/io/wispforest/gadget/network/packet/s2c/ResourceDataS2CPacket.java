package io.wispforest.gadget.network.packet.s2c;

import net.minecraft.util.Identifier;

public record ResourceDataS2CPacket(Identifier id, byte[] data) {
}
