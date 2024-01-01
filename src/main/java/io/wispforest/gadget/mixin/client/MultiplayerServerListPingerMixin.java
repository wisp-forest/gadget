package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.dump.ClientPacketDumper;
import net.minecraft.client.network.MultiplayerServerListPinger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerServerListPinger.class)
public class MultiplayerServerListPingerMixin {

    @Inject(method = "cancel", at = @At(value = "HEAD"))
    private void stopDumpingOnCancel(CallbackInfo ci) {
        if (ClientPacketDumper.isDumping()) {
            ClientPacketDumper.stop();
        }
    }

    @Mixin(targets = "net/minecraft/client/network/MultiplayerServerListPinger$1")
    public static class ClientQueryPacketListenerImplMixin {

        @Inject(method = "onPingResult", at = @At("HEAD"))
        private void stopDumpingOnPingResult(CallbackInfo ci) {
            if (ClientPacketDumper.isDumping()) {
                ClientPacketDumper.stop();
            }
        }

        @Inject(method = "onDisconnected", at = @At("HEAD"))
        private void stopDumpingOnDisconnected(CallbackInfo ci) {
            if (ClientPacketDumper.isDumping()) {
                ClientPacketDumper.stop();
            }
        }

    }

}
