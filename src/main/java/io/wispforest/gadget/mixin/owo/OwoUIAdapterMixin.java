package io.wispforest.gadget.mixin.owo;

import io.wispforest.gadget.client.gui.ComponentEventCounter;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OwoUIAdapter.class)
public class OwoUIAdapterMixin {
    @Inject(method = "render", at = @At("HEAD"))
    private void reset(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ComponentEventCounter.reset();
    }

    @Inject(method = {"mouseClicked", "mouseReleased", "mouseScrolled", "mouseDragged", "keyPressed", "charTyped"}, at = @At("HEAD"))
    private void reset(CallbackInfoReturnable<?> cir) {
        ComponentEventCounter.reset();
    }

    @Inject(method = {"mouseClicked", "mouseReleased", "mouseScrolled", "mouseDragged", "keyPressed", "charTyped"}, at = @At("RETURN"))
    private void tally(CallbackInfoReturnable<Boolean> cir) {
        ComponentEventCounter.tally();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void tally(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ComponentEventCounter.tally();
    }
}
