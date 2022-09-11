package me.basiqueevangelist.gadget.util;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestS2CPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

public final class NetworkUtil {
    private NetworkUtil() {

    }

    public static Identifier getChannelOrNull(Packet<?> packet) {
        if (packet instanceof CustomPayloadS2CPacket pkt)
            return pkt.getChannel();
        else if (packet instanceof CustomPayloadC2SPacket pkt)
            return pkt.getChannel();
        else if (packet instanceof LoginQueryRequestS2CPacket pkt)
            return pkt.getChannel();
        else
            return null;
    }
}
