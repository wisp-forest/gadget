package io.wispforest.gadget.mixin.fabric;

import net.fabricmc.fabric.impl.networking.payload.RetainedPayload;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(RetainedPayload.class)
public class RetainedPayloadMixin {
    @Shadow @Final private PacketByteBuf buf;

    @Inject(method = "write", at = @At("HEAD"), require = 0, cancellable = true)
    private void noItShould(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeBytes(this.buf.slice());
        ci.cancel();
    }
}
