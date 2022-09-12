package me.basiqueevangelist.gadget.client.dump.handler;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Insets;
import me.basiqueevangelist.gadget.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class FapiSupport {
    private static final Identifier EARLY_REGISTRATION_CHANNEL = new Identifier("fabric-networking-api-v1", "early_registration");

    private FapiSupport() {

    }

    public static void init() {
        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (!Objects.equals(packet.channelId(), EARLY_REGISTRATION_CHANNEL)) return false;

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

        // TODO: Registry sync.
    }
}
