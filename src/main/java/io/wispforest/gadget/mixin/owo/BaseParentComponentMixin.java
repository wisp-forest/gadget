package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.client.gui.ComponentEventCounter;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseParentComponent.class)
public abstract class BaseParentComponentMixin extends BaseComponent {
    @Inject(method = "onChildMutated", at = @At("HEAD"), remap = false)
    private void mald(Component child, CallbackInfo ci) {
        if (mounted) {
            ComponentEventCounter.countMutation();
        }
    }
}
