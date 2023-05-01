package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.dump.read.handler.OwoSupport;
import io.wispforest.gadget.field.LocalFieldDataSource;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Insets;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class ClientOwoSupport {
    private ClientOwoSupport() {

    }

    public static void init() {
        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            OwoSupport.ParticleSystemPacket parsed = OwoSupport.parseParticleSystemPacket(packet);

            if (parsed == null) return false;

            view.child(Components.label(
                Text.translatable("text.gadget.particle_system", parsed.systemId(), (int) parsed.pos().x, (int) parsed.pos().y, (int) parsed.pos().z)));

            if (parsed.data() != null) {
                FieldDataIsland island = new FieldDataIsland(
                    new LocalFieldDataSource(parsed.data(), false),
                    true,
                    false
                );
                view.child(island.mainContainer());
            }

            return true;
        });

        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            Map<Identifier, Integer> handshakeReq = OwoSupport.parseHandshakeRequest(packet);

            if (handshakeReq == null) return false;

            drawHandshakeMap(handshakeReq, Text.literal("o ").formatted(Formatting.AQUA), view);

            return true;
        });

        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            OwoSupport.HandshakeResponse response = OwoSupport.parseHandshakeResponse(packet);

            if (response == null) return false;

            drawHandshakeMap(response.requiredChannels(), Text.literal("r ").formatted(Formatting.RED), view);
            drawHandshakeMap(response.requiredControllers(), Text.literal("p ").formatted(Formatting.GREEN), view);
            drawHandshakeMap(response.optionalChannels(), Text.literal("o ").formatted(Formatting.AQUA), view);

            return true;
        });

        // TODO: OwO handshake and config sync.
    }

    private static void drawHandshakeMap(Map<Identifier, Integer> data, Text prefix, FlowLayout view) {
        for (var entry : data.entrySet()) {
            view.child(Components.label(
                    Text.literal("")
                        .append(prefix)
                        .append(Text.literal(entry.getKey().toString())
                            .formatted(Formatting.WHITE))
                        .append(Text.literal(" = " + entry.getValue())
                            .formatted(Formatting.GRAY)))
                .margins(Insets.bottom(3)));
        }
    }
}