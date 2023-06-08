package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.client.gui.ComponentEventCounter;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.Size;
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
}
