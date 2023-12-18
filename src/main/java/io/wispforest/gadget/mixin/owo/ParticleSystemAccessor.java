package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.particles.systems.ParticleSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ParticleSystem.class, remap = false)
public interface ParticleSystemAccessor {
    @Accessor
    Endec<?> getEndec();
}
