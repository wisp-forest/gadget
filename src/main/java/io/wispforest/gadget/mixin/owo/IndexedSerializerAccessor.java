package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.network.serialization.RecordSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "io/wispforest/owo/network/OwoNetChannel$IndexedSerializer", remap = false)
public interface IndexedSerializerAccessor {
    @Accessor
    RecordSerializer<?> getSerializer();
}
