package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.Gadget;
import net.minecraft.client.util.ProfileKeysImpl;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProfileKeysImpl.class)
public class ProfileKeysImplMixin {
    @Mutable @Shadow @Final private static Logger LOGGER;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void silenceLogger(CallbackInfo ci) {
        if (!Gadget.CONFIG.silenceStartupErrors()) return;

        LOGGER = NOPLogger.NOP_LOGGER;
    }
}
