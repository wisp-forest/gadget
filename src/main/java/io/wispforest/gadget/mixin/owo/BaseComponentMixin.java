package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.client.gui.ComponentEventCounter;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BaseComponent.class, remap = false)
public class BaseComponentMixin {
    @Inject(method = "inflate", at = @At("HEAD"))
    private void mald(Size space, CallbackInfo ci) {
        ComponentEventCounter.countInflation();
    }

    @Inject(method = "dismount", at = @At("HEAD"))
    private void breh(Component.DismountReason reason, CallbackInfo ci) {
        if (Screen.hasShiftDown()) {
            new Throwable("breeeh on " + Thread.currentThread().getName()).printStackTrace();
        }
    }
}
