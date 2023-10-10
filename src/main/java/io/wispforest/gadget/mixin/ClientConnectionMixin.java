package io.wispforest.gadget.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
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
    @Shadow private volatile PacketListener packetListener;

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"))
    private void readHook(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) return;
        
        ClientPacketDumper.dump(packet, packetListener, NetworkSide.CLIENTBOUND);
    }

    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void writeHook(Packet<?> packet, @Nullable PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
        if (side == NetworkSide.SERVERBOUND) return;
        
        ClientPacketDumper.dump(packet, packetListener, NetworkSide.SERVERBOUND);
    }
}
