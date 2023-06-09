package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.MatrixStackLogger;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import io.wispforest.gadget.client.dump.DumpPrimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void tripModelViewStack(boolean tick, CallbackInfo ci) {
        MatrixStackLogger.startLoggingIfNeeded();
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        if (DumpPrimer.isPrimed) {
            ClientPacketDumper.start(false);

            DumpPrimer.isPrimed = false;
        } else if (ClientPacketDumper.isDumping()) {
            ClientPacketDumper.stop();
        }
    }

    @WrapWithCondition(method = "createUserApiService", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false))
    private boolean shhhh(Logger logger, String text, Throwable throwable) {
        return !Gadget.CONFIG.silenceStartupErrors();
    }
}
