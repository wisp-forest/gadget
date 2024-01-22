package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.unwrapped.FieldsUnwrappedPacket;
import io.wispforest.gadget.dump.read.unwrapped.LinesUnwrappedPacket;
import io.wispforest.gadget.network.FabricPacketHacks;
import io.wispforest.gadget.util.ErrorSink;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class FapiSupport {
    public static final Identifier EARLY_REGISTRATION_CHANNEL = new Identifier("fabric-networking-api-v1", "early_registration");

    private FapiSupport() {

    }

    public static void init() {
        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            PacketType<?> type = FabricPacketHacks.getForId(packet.channelId());

            if (type == null) return null;

            PacketByteBuf buf = packet.wrappedBuf();
            Object unwrapped = type.read(buf);

            return new FabricObjectPacket(unwrapped);
        });

        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (!Objects.equals(packet.channelId(), EARLY_REGISTRATION_CHANNEL)) return null;

            PacketByteBuf buf = packet.wrappedBuf();
            List<Identifier> channels = buf.readList(PacketByteBuf::readIdentifier);

            return new EarlyRegisterPacket(channels);
        });
    }

    public record FabricObjectPacket(Object unwrapped) implements FieldsUnwrappedPacket {
        @Override
        public @Nullable Object rawFieldsObject() {
            return unwrapped;
        }
    }

    public record EarlyRegisterPacket(List<Identifier> channels) implements LinesUnwrappedPacket {
        @Override
        public void render(Consumer<Text> out, ErrorSink errSink) {
            for (Identifier channel : channels) {
                out.accept(
                    Text.literal("+ ")
                        .formatted(Formatting.GREEN)
                        .append(Text.literal(channel.toString())
                            .formatted(Formatting.GRAY)));
            }
        }
    }
}
