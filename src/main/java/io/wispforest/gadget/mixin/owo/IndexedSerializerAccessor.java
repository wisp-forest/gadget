package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.serialization.StructEndec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "io/wispforest/owo/network/OwoNetChannel$IndexedSerializer", remap = false)
public interface IndexedSerializerAccessor {
    @Accessor
    StructEndec<?> getSerializer();
}
