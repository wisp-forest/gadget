package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.gui.VanillaInspector;
import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Inject(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"))
    private void onKeyPressed(int i, Screen screen, boolean[] bls, int j, int k, int l, CallbackInfo ci) {
        VanillaInspector inspector = VanillaInspector.get(screen);

        if (inspector == null) return;

        inspector.keyPressed(screen, j, k, l);
    }
}
