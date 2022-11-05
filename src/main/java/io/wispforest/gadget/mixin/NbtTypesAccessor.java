package io.wispforest.gadget.mixin;

import net.minecraft.nbt.NbtType;
import net.minecraft.nbt.NbtTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NbtTypes.class)
public interface NbtTypesAccessor {
    @Accessor
    static NbtType<?>[] getVALUES() {
        throw new UnsupportedOperationException();
    }
}
