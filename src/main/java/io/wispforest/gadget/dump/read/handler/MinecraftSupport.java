package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.unwrapped.LinesUnwrappedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class MinecraftSupport {
    public static final Identifier REGISTER_CHANNEL = new Identifier("minecraft", "register");

    public static final Identifier UNREGISTER_CHANNEL = new Identifier("minecraft", "unregister");

    private MinecraftSupport() {

    }

    public static void init() {
        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            if (!Objects.equals(packet.channelId(), BrandCustomPayload.ID)) return null;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            String brand = buf.readString();

            return new BrandPacket(brand);
        });

        PacketUnwrapper.EVENT.register((packet, errSink) -> {
            boolean unregister;

            if (Objects.equals(packet.channelId(), REGISTER_CHANNEL)) {
                unregister = false;
            } else if (Objects.equals(packet.channelId(), UNREGISTER_CHANNEL)) {
                unregister = true;
            } else {
                return null;
            }

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            StringBuilder more = new StringBuilder();
            List<Identifier> channels = new ArrayList<>();

            while (buf.isReadable()) {
                byte next = buf.readByte();

                if (next != 0) {
                    more.append((char) next);
                } else {
                    channels.add(new Identifier(more.toString()));

                    more = new StringBuilder();
                }
            }

            return new RegisterPacket(unregister, channels);
        });
    }

    public record BrandPacket(String brand) implements LinesUnwrappedPacket {
        @Override
        public void render(Consumer<Text> out, ErrorSink errSink) {
            out.accept(
                Text.literal("brand")
                    .append(Text.literal(" = \"" + brand + "\"")
                        .formatted(Formatting.GRAY)));
        }
    }

    public record RegisterPacket(boolean isUnregister, List<Identifier> channels) implements LinesUnwrappedPacket {
        @Override
        public void render(Consumer<Text> out, ErrorSink errSink) {
            Text header = !isUnregister
                ? Text.literal("+ ")
                    .formatted(Formatting.GREEN)
                : Text.literal("- ")
                    .formatted(Formatting.RED);

            for (Identifier channel : channels) {
                out.accept(
                    Text.literal("")
                        .append(header)
                        .append(Text.literal(channel.toString())
                            .formatted(Formatting.GRAY)));
            }
        }
    }
}
