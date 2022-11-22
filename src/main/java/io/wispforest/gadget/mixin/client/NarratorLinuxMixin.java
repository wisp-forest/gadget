package io.wispforest.gadget.mixin.client;

import com.mojang.text2speech.NarratorLinux;
import io.wispforest.gadget.Gadget;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NarratorLinux.class, remap = false)
public class NarratorLinuxMixin {
    @Mutable @Shadow @Final private static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/sun/jna/Native;register(Ljava/lang/Class;Lcom/sun/jna/NativeLibrary;)V"))
    private static void shhhh(CallbackInfo ci) {
        if (!Gadget.CONFIG.silenceStartupErrors()) return;

        LOGGER = NOPLogger.NOP_LOGGER;
    }
}
