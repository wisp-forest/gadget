package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.dump.read.handler.MinecraftSupport;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Objects;

public final class ClientMinecraftSupport {
    private ClientMinecraftSupport() {

    }

    public static void init() {
        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            if (!Objects.equals(packet.channelId(), CustomPayloadS2CPacket.BRAND)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            String brand = buf.readString();

            view.child(Components.label(Text.literal("brand")
                .append(Text.literal(" = \"" + brand + "\"")
                    .formatted(Formatting.GRAY))));

            return true;
        });

        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            MinecraftSupport.RegisterPacket parsed = MinecraftSupport.parseRegisterPacket(packet);

            if (parsed == null) return false;

            Text header = !parsed.unregister()
                ? Text.literal("+ ")
                    .formatted(Formatting.GREEN)
                : Text.literal("- ")
                    .formatted(Formatting.RED);

            for (Identifier channel : parsed.channels()) {
                view.child(Components.label(
                        Text.literal("")
                            .append(header)
                            .append(Text.literal(channel.toString())
                                .formatted(Formatting.GRAY))
                    )
                    .margins(Insets.bottom(3)));
            }

            return true;
        });

        // TODO: debug packets. maybe.
    }
}
