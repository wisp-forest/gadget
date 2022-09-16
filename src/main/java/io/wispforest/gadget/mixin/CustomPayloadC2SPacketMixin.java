package io.wispforest.gadget.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
    @Shadow @Final private PacketByteBuf data;

    private int gadget$stashedReaderIndex = 0;

    @Inject(method = "write", at = @At("HEAD"))
    private void stashReaderIndex(PacketByteBuf buf, CallbackInfo ci) {
        gadget$stashedReaderIndex = data.readerIndex();
    }

    @Inject(method = "write", at = @At("RETURN"))
    private void restoreReaderIndex(PacketByteBuf buf, CallbackInfo ci) {
        data.readerIndex(gadget$stashedReaderIndex);
    }
}
