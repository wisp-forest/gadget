package me.basiqueevangelist.gadget.client.dump.handler;

import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.util.VectorSerializer;
import me.basiqueevangelist.gadget.client.field.FieldDataIsland;
import me.basiqueevangelist.gadget.mixin.owo.OwoNetChannelAccessor;
import me.basiqueevangelist.gadget.mixin.owo.ParticleSystemAccessor;
import me.basiqueevangelist.gadget.util.NetworkUtil;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public final class OwoSupport {
    public static final Identifier HANDSHAKE_CHANNEL = new Identifier("owo", "handshake");

    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Map<Identifier, Integer>> HANDSHAKE_SERIALIZER =
        (PacketBufSerializer<Map<Identifier, Integer>>) (Object) PacketBufSerializer.createMapSerializer(Map.class, Identifier.class, Integer.class);

    private OwoSupport() {

    }

    public static void init() {
        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (packet.state() != NetworkState.PLAY) return false;

            if (packet.channelId() == null) return false;

            OwoNetChannelAccessor channel = (OwoNetChannelAccessor) OwoNetChannelAccessor.getRegisteredChannels().get(packet.channelId());

            if (channel == null) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int handlerId = buf.readVarInt();

            if (!packet.outbound())
                handlerId = -handlerId;

            RecordSerializer<?> serializer = channel.getSerializersByIndex().get(handlerId).getSerializer();

            Object unwrapped = serializer.read(buf);

            view.child(Components.label(Text.literal(ReflectionUtil.nameWithoutPackage(unwrapped.getClass()))));

            FieldDataIsland island = new FieldDataIsland();

            island.shortenNames();
            island.targetObject(unwrapped, false);

            view.child(island.mainContainer());

            return true;
        });

        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (packet.state() != NetworkState.PLAY) return false;

            if (packet.channelId() == null) return false;

            ParticleSystemController controller = ParticleSystemController.REGISTERED_CONTROLLERS.get(packet.channelId());

            if (controller == null) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int systemId = buf.readVarInt();
            Vec3d pos = VectorSerializer.read(buf);

            view.child(Components.label(Text.translatable("text.gadget.particle_system", systemId, (int) pos.x, (int) pos.y, (int) pos.z)));

            ParticleSystem<?> system = controller.systemsByIndex.get(systemId);
            Object data = ((ParticleSystemAccessor) system).getAdapter().deserializer().apply(buf);

            if (data != null) {
                FieldDataIsland island = new FieldDataIsland();
                island.shortenNames();
                island.targetObject(data, false);
                view.child(island.mainContainer());
            }

            return true;
        });

        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (!(packet.packet() instanceof LoginQueryRequestS2CPacket) || !Objects.equals(packet.channelId(), HANDSHAKE_CHANNEL)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());

            if (buf.isReadable()) {
                Map<Identifier, Integer> optionalChannels = HANDSHAKE_SERIALIZER.deserializer().apply(buf);

                drawHandshakeMap(optionalChannels, Text.literal("o ").formatted(Formatting.AQUA), view);
            }

            return true;
        });

        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (!(packet.packet() instanceof LoginQueryResponseC2SPacket) || !Objects.equals(packet.channelId(), HANDSHAKE_CHANNEL)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());

            Map<Identifier, Integer> requiredChannels = HANDSHAKE_SERIALIZER.deserializer().apply(buf);
            Map<Identifier, Integer> requiredControllers = HANDSHAKE_SERIALIZER.deserializer().apply(buf);

            drawHandshakeMap(requiredChannels, Text.literal("r ").formatted(Formatting.RED), view);
            drawHandshakeMap(requiredControllers, Text.literal("p ").formatted(Formatting.GREEN), view);

            if (buf.isReadable()) {
                Map<Identifier, Integer> optionalChannels = HANDSHAKE_SERIALIZER.deserializer().apply(buf);

                drawHandshakeMap(optionalChannels, Text.literal("o ").formatted(Formatting.AQUA), view);
            }

            return true;
        });

        // TODO: OwO handshake and config sync.
    }

    private static void drawHandshakeMap(Map<Identifier, Integer> data, Text prefix, VerticalFlowLayout view) {
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
