package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.gui.VanillaInspector;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract boolean processF3(int key);

    @Inject(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"))
    private void onKeyPressed(int i, Screen screen, boolean[] bls, int j, int k, int l, CallbackInfo ci) {
        VanillaInspector inspector = VanillaInspector.get(screen);

        if (inspector == null) return;

        inspector.keyPressed(screen, j, k, l);
    }

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void doF3(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window != client.getWindow().getHandle()) return;
        if (client.currentScreen == null) return;
        if (action != GLFW.GLFW_PRESS) return;

        if (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_F3) && processF3(key)) {
            ci.cancel();
        }
    }

    @Inject(method = "processF3", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;"), cancellable = true)
    private void leaveIfPlayer(int key, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null)
            cir.setReturnValue(false);
    }

    @Inject(method = "processF3", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasPermissionLevel(I)Z"), cancellable = true)
    private void leaveOnGameModeSelection(int key, CallbackInfoReturnable<Boolean> cir) {
        if (client.player == null)
            cir.setReturnValue(false);
    }
}
