package io.wispforest.gadget.nbt;

import net.minecraft.item.ItemStack;

public class NbtLocks {
    public static final NbtLock EMPTY = () -> "NbtLocks#EMPTY";
    public static final NbtLock GET_NAME = () -> "NbtLocks#GET_NAME";

    public static void init() {
        ((LockableNbt)(Object) ItemStack.EMPTY).lock(EMPTY);
    }
}
