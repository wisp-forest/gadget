package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.Gadget;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Component.class, remap = false)
public interface ComponentMixin {
    @Shadow @Nullable ParentComponent parent();

    @Inject(method = "remove", at = @At("HEAD"), cancellable = true)
    private void remove(CallbackInfo ci) {
        if (!Gadget.CONFIG.errorCheckOwoUi()) return;

        ci.cancel();

        ParentComponent currentParent = parent();

        if (currentParent == null) return;

        Throwable context = new Throwable("Component#remove was called here");

        currentParent.queue(() -> {
            ParentComponent newParent = parent();

            if (newParent == currentParent) {
                currentParent.removeChild((Component) this);
            } else {
                throw new IllegalStateException("Component " + this + "'s parent changed from " + currentParent + " to " + newParent + " after Component$remove() call", context);
            }
        });
    }
}
