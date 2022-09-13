package me.basiqueevangelist.gadget.mixin.client;

import me.basiqueevangelist.gadget.client.dump.DumpPrimer;
import me.basiqueevangelist.gadget.client.dump.PacketDumper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        if (DumpPrimer.isPrimed) {
            PacketDumper.start(false);

            DumpPrimer.isPrimed = false;
        } else if (PacketDumper.isDumping()) {
            PacketDumper.stop();
        }
    }
}
