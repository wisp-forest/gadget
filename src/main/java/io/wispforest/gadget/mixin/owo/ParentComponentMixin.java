package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.gadget.client.gui.BasedScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ParentComponent.class, remap = false)
public interface ParentComponentMixin extends Component {
    @SuppressWarnings({"ConstantConditions", "RedundantCast"})
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"), cancellable = true)
    private void cancel(float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.getClass() == (Class<?>) BasedScrollContainer.class)
            ci.cancel();
    }
}
