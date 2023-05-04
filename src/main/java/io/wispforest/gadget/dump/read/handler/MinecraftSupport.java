package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class MinecraftSupport {
    public static final Identifier REGISTER_CHANNEL = new Identifier("minecraft", "register");

    public static final Identifier UNREGISTER_CHANNEL = new Identifier("minecraft", "unregister");

    private MinecraftSupport() {

    }

    public static void init() {
        PlainTextPacketDumper.EVENT.register((packet, out, indent, errSink) -> {
            if (!Objects.equals(packet.channelId(), CustomPayloadS2CPacket.BRAND)) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            String brand = buf.readString();

            out.write(indent, "brand = \"" + brand + "\"");

            return true;
        });

        PlainTextPacketDumper.EVENT.register((packet, out, indent, errSink) -> {
            MinecraftSupport.RegisterPacket parsed = MinecraftSupport.parseRegisterPacket(packet);

            if (parsed == null) return false;

            for (Identifier channel : parsed.channels()) {
                out.write(indent, (parsed.unregister() ? "+ " : "- ") + channel);
            }

            return true;
        });
    }

    public static @Nullable RegisterPacket parseRegisterPacket(DumpedPacket packet) {
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
    }

    public record RegisterPacket(boolean unregister, List<Identifier> channels) { }
}
