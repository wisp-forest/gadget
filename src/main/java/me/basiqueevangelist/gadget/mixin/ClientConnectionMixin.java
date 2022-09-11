package me.basiqueevangelist.gadget.mixin;

import io.netty.channel.ChannelHandlerContext;
import me.basiqueevangelist.gadget.client.dump.PacketDumper;
import net.minecraft.network.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
    @Shadow @Final private NetworkSide side;

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"))
    private void readHook(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) return;

        if (PacketDumper.isDumping()) {
            PacketDumper.dump(false, packet);
        }
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void writeHook(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) return;

        if (PacketDumper.isDumping()) {
            PacketDumper.dump(true, packet);
        }
    }
}
