package io.wispforest.gadget.network.packet.c2s;

import net.minecraft.util.Identifier;

public record RequestResourceC2SPacket(Identifier id, int index) {
}
