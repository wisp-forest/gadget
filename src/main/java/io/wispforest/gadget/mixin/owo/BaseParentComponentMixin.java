package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.client.gui.ComponentEventCounter;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.base.BaseParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BaseParentComponent.class)
public abstract class BaseParentComponentMixin extends BaseComponent {
    @Shadow @Nullable public abstract FocusHandler focusHandler();

    @Inject(method = "onChildMutated", at = @At("HEAD"), remap = false)
    private void mald(Component child, CallbackInfo ci) {
        if (mounted) {
            ComponentEventCounter.countMutation();
        }
    }

    @Inject(method = "drawChildren", at = @At(value = "INVOKE", target = "Lio/wispforest/owo/ui/base/BaseParentComponent;focusHandler()Lio/wispforest/owo/ui/util/FocusHandler;"))
    private void breh(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta, List<Component> children, CallbackInfo ci) {
        if (focusHandler() == null) {
            throw new IllegalStateException("focus handler null? wtf");
        }
    }
}
