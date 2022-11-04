package io.wispforest.gadget.network;

import net.minecraft.item.ItemStack;

public record ReplaceStackC2SPacket(int slotId, ItemStack stack) {
}
