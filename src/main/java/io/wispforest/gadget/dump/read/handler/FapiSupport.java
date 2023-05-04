package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.network.FabricPacketHacks;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class FapiSupport {
    public static final Identifier EARLY_REGISTRATION_CHANNEL = new Identifier("fabric-networking-api-v1", "early_registration");

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

        PlainTextPacketDumper.EVENT.register((packet, out, indent, errSink) -> {
            if (!Objects.equals(packet.channelId(), EARLY_REGISTRATION_CHANNEL)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int count = buf.readVarInt();

            for (int i = 0; i < count; i++) {
                Identifier channel = buf.readIdentifier();

                out.write(indent, "+ " + channel.toString());
            }

            return true;
        });
    }
}
