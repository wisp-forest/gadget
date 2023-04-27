package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.network.FabricPacketHacks;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;

public final class FapiSupport {
    private FapiSupport() {

    }

    public static void init() {
        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            PacketType<?> type = FabricPacketHacks.getForId(packet.channelId());

            if (type == null) return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            Object unwrapped = type.read(buf);

            return new PacketUnwrapper.Unwrapped(unwrapped);
        });
    }
}
