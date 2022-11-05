package io.wispforest.gadget.desc;

import net.minecraft.nbt.NbtCompound;

public record NbtCompoundFieldObject(NbtCompound data) implements FieldObject {
    @Override
    public String type() {
        return "n";
    }

    @Override
    public int color() {
        return 0xFF0000;
    }
}
