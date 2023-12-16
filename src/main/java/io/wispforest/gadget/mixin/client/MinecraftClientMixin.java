package io.wispforest.gadget.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.MatrixStackLogger;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import io.wispforest.gadget.client.dump.DumpPrimer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow private volatile boolean paused;

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

    @Inject(method = "render", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/MinecraftClient;paused:Z", shift = At.Shift.AFTER))
    private void flushDump(boolean tick, CallbackInfo ci) {
        if (this.paused)
            ClientPacketDumper.flushIfNeeded();
    }
}
