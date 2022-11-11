package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.MatrixStackLogger;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void checkMatrixStack(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo ci) {
        if (!matrices.isEmpty()) {
            MatrixStackLogger.tripError(matrices, "Matrix stack not empty");
        }
    }
}
