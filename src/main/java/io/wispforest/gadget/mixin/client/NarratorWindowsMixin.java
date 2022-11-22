package io.wispforest.gadget.mixin.client;

import com.mojang.text2speech.NarratorWindows;
import io.wispforest.gadget.Gadget;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NarratorWindows.class, remap = false)
public class NarratorWindowsMixin {
    @Mutable @Shadow @Final private static Logger LOGGER;

    @Inject(method = "tryLoadNative", at = @At("HEAD"))
    private static void shhhh(CallbackInfoReturnable<Boolean> cir) {
        if (!Gadget.CONFIG.silenceStartupErrors()) return;

        LOGGER = NOPLogger.NOP_LOGGER;
    }
}
