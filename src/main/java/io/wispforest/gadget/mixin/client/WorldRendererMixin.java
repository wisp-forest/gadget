package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.MatrixStackLogger;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "checkEmpty", at = @At("HEAD"), cancellable = true)
    private void checkEmpty(MatrixStack matrices, CallbackInfo ci) {
        if (!matrices.isEmpty()) {
            MatrixStackLogger.tripError(matrices, "Matrix stack not empty");
            ci.cancel();
        }
    }
}
