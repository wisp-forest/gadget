package io.wispforest.gadget.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
    private int gadget$stashedReaderIndex = 0;

    @Inject(method = "write", at = @At(value = "INVOKE", 
              target = "Lnet/minecraft/network/packet/CustomPayload;write(Lnet/minecraft/network/PacketByteBuf;)V", 
              shift = At.Shift.BEFORE))
    private void stashReaderIndex(PacketByteBuf buf, CallbackInfo ci) {
        gadget$stashedReaderIndex = buf.readerIndex();
    }

    @Inject(method = "write", at = @At(value = "INVOKE", 
              target = "Lnet/minecraft/network/packet/CustomPayload;write(Lnet/minecraft/network/PacketByteBuf;)V", 
              shift = At.Shift.AFTER))
    private void restoreReaderIndex(PacketByteBuf buf, CallbackInfo ci) {
        buf.readerIndex(gadget$stashedReaderIndex);
    }
}
