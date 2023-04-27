package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.mixin.owo.IndexedSerializerAccessor;
import io.wispforest.gadget.mixin.owo.OwoNetChannelAccessor;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;

public final class OwoSupport {
    private OwoSupport() {

    }

    public static void init() {
        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (packet.state() != NetworkState.PLAY) return null;

            if (packet.channelId() == null) return null;

            OwoNetChannelAccessor channel = (OwoNetChannelAccessor) OwoNetChannelAccessor.getRegisteredChannels().get(packet.channelId());

            if (channel == null) return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int netHandlerId = buf.readVarInt();
            int handlerId = netHandlerId;

            if (!packet.outbound())
                handlerId = -handlerId;

            IndexedSerializerAccessor acc = channel.getSerializersByIndex().get(handlerId);

            if (acc == null) return null;

            Object unwrapped = acc.getSerializer().read(buf);

            return new PacketUnwrapper.Unwrapped(unwrapped, netHandlerId);
        });
    }
}
