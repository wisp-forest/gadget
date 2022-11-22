package io.wispforest.gadget.network.packet.s2c;

import net.minecraft.util.Identifier;

import java.util.Map;

public record ResourceListS2CPacket(Map<Identifier, Integer> resources) {
}
