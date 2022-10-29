package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.mappings.MappingsManager;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashOverlay.class)
public class SplashOverlayMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReload;throwException()V", shift = At.Shift.AFTER))
    private void tryInitMappings(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MappingsManager.init();
    }
}
