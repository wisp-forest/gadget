package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.dump.read.unwrapped.FieldsUnwrappedPacket;
import io.wispforest.gadget.dump.read.unwrapped.LinesUnwrappedPacket;
import io.wispforest.gadget.mixin.owo.IndexedSerializerAccessor;
import io.wispforest.gadget.mixin.owo.OwoNetChannelAccessor;
import io.wispforest.gadget.mixin.owo.ParticleSystemAccessor;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.util.VectorSerializer;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;

public final class OwoSupport {
    public static final Identifier HANDSHAKE_CHANNEL = new Identifier("owo", "handshake");
    @SuppressWarnings("unchecked")
    private static final PacketBufSerializer<Map<Identifier, Integer>> HANDSHAKE_SERIALIZER =
        (PacketBufSerializer<Map<Identifier, Integer>>) (Object) PacketBufSerializer.createMapSerializer(Map.class, Identifier.class, Integer.class);

    private OwoSupport() {

    }

    @SuppressWarnings("UnstableApiUsage")
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

            return new ChannelPacket(unwrapped, netHandlerId);
        });

        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (packet.state() != NetworkState.PLAY || packet.channelId() == null) return null;

            ParticleSystemController controller = ParticleSystemController.REGISTERED_CONTROLLERS.get(packet.channelId());

            if (controller == null) return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int systemId = buf.readVarInt();
            Vec3d pos = VectorSerializer.read(buf);
            ParticleSystem<?> system = controller.systemsByIndex.get(systemId);
            Object data = ((ParticleSystemAccessor) system).getAdapter().deserializer().apply(buf);

            return new ParticleSystemPacket(controller, systemId, pos, data);
        });

        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (!(packet.packet() instanceof LoginQueryRequestS2CPacket)
             || !Objects.equals(packet.channelId(), HANDSHAKE_CHANNEL))
                return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());

            return new HandshakeRequest(buf.isReadable()
                ? HANDSHAKE_SERIALIZER.deserializer().apply(buf)
                : Collections.emptyMap());
        });

        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (!(packet.packet() instanceof LoginQueryResponseC2SPacket)
             || !Objects.equals(packet.channelId(), HANDSHAKE_CHANNEL))
                return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());

            Map<Identifier, Integer> requiredChannels = HANDSHAKE_SERIALIZER.deserializer().apply(buf);
            Map<Identifier, Integer> requiredControllers = HANDSHAKE_SERIALIZER.deserializer().apply(buf);

            Map<Identifier, Integer> optionalChannels = Collections.emptyMap();

            if (buf.isReadable())
                optionalChannels = HANDSHAKE_SERIALIZER.deserializer().apply(buf);

            return new HandshakeResponse(requiredChannels, requiredControllers, optionalChannels);
        });

    }

    private static void drawHandshakeMap(Map<Identifier, Integer> data, Text prefix, Consumer<Text> out) {
        for (var entry : data.entrySet()) {
            out.accept(Text.literal("")
                .append(prefix)
                .append(Text.literal(entry.getKey().toString())
                    .formatted(Formatting.WHITE))
                .append(Text.literal(" = " + entry.getValue())
                    .formatted(Formatting.GRAY)));
        }
    }

    public record ParticleSystemPacket(ParticleSystemController controller, int systemId, Vec3d pos, Object data) implements FieldsUnwrappedPacket {
        @Override
        public Text headText() {
            return Text.translatable("text.gadget.particle_system", systemId, (int) pos.x, (int) pos.y, (int) pos.z);
        }

        @Override
        public @Nullable Object rawFieldsObject() {
            return data;
        }

        @Override
        public OptionalInt packetId() {
            return OptionalInt.of(systemId);
        }
    }

    public record ChannelPacket(Object packetData, int channelPacketId) implements FieldsUnwrappedPacket {
        @Override
        public @Nullable Object rawFieldsObject() {
            return packetData;
        }

        @Override
        public OptionalInt packetId() {
            return OptionalInt.of(channelPacketId);
        }
    }

    public record HandshakeRequest(Map<Identifier, Integer> optionalChannels) implements LinesUnwrappedPacket {
        @Override
        public void render(Consumer<Text> out, ErrorSink errSink) {
            drawHandshakeMap(optionalChannels, Text.literal("o ").formatted(Formatting.AQUA), out);
        }
    }

    public record HandshakeResponse(Map<Identifier, Integer> requiredChannels,
                                    Map<Identifier, Integer> requiredControllers,
                                    Map<Identifier, Integer> optionalChannels) implements LinesUnwrappedPacket {
        @Override
        public void render(Consumer<Text> out, ErrorSink errSink) {
            drawHandshakeMap(requiredChannels, Text.literal("r ").formatted(Formatting.RED), out);
            drawHandshakeMap(requiredControllers, Text.literal("p ").formatted(Formatting.GREEN), out);
            drawHandshakeMap(optionalChannels, Text.literal("o ").formatted(Formatting.AQUA), out);
        }
    }
}
