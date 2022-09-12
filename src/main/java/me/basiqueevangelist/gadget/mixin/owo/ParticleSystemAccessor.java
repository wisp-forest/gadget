package me.basiqueevangelist.gadget.mixin.owo;

import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.particles.systems.ParticleSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ParticleSystem.class)
public interface ParticleSystemAccessor {
    @Accessor
    PacketBufSerializer<?> getAdapter();
}
