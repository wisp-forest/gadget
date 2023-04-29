package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.text2speech.Narrator;
import io.wispforest.gadget.Gadget;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Narrator.class, remap = false)
public interface NarratorMixin {
    @WrapWithCondition(method = "getNarrator", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"))
    private static boolean shhhh(Logger logger, String message, Throwable throwable) {
        return !Gadget.CONFIG.silenceStartupErrors();
    }
}
