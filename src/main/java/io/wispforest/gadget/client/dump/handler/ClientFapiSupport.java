package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.dump.read.handler.FapiSupport;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class ClientFapiSupport {
    private ClientFapiSupport() {

    }

    public static void init() {
        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            if (!Objects.equals(packet.channelId(), FapiSupport.EARLY_REGISTRATION_CHANNEL)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int count = buf.readVarInt();

            for (int i = 0; i < count; i++) {
                Identifier channel = buf.readIdentifier();

                view.child(Components.label(
                        Text.literal("+ ")
                            .formatted(Formatting.GREEN)
                            .append(Text.literal(channel.toString())
                                .formatted(Formatting.GRAY))
                    )
                    .margins(Insets.bottom(3)));
            }

            return true;
        });
    }
}
