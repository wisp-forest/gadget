package io.wispforest.gadget.client.dump.handler;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class MinecraftSupport {
    public static final Identifier REGISTER_CHANNEL = new Identifier("minecraft", "register");

    public static final Identifier UNREGISTER_CHANNEL = new Identifier("minecraft", "unregister");

    private MinecraftSupport() {

    }

    public static void init() {
        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (!Objects.equals(packet.channelId(), CustomPayloadS2CPacket.BRAND)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            String brand = buf.readString();

            view.child(Components.label(Text.literal("brand")
                .append(Text.literal(" = \"" + brand + "\"")
                    .formatted(Formatting.GRAY))));

            return true;
        });

        DrawPacketHandler.EVENT.register((packet, view) -> {
            Text header;

            if (Objects.equals(packet.channelId(), REGISTER_CHANNEL)) {
                header = Text.literal("+ ")
                    .formatted(Formatting.GREEN);
            } else if (Objects.equals(packet.channelId(), UNREGISTER_CHANNEL)) {
                header = Text.literal("- ")
                    .formatted(Formatting.RED);
            } else {
                return false;
            }

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            StringBuilder more = new StringBuilder();

            while (buf.isReadable()) {
                byte next = buf.readByte();

                if (next != 0) {
                    more.append((char) next);
                } else {
                    view.child(Components.label(
                            Text.literal("")
                                .append(header)
                                .append(Text.literal(more.toString())
                                    .formatted(Formatting.GRAY))
                        )
                        .margins(Insets.bottom(3)));

                    more = new StringBuilder();
                }

            }

            return true;
        });

        // TODO: debug packets. maybe.
    }
}
