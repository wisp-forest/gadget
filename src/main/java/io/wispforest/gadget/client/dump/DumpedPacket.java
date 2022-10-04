package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ReflectionUtil;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySetHeadYawS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Identifier;

import java.util.Set;

public record DumpedPacket(boolean outbound, NetworkState state, Packet<?> packet, Identifier channelId) {
    private static final Set<Class<?>> FREQUENT_PACKET_TYPES = Set.of(
        EntityS2CPacket.MoveRelative.class, EntityS2CPacket.Rotate.class, EntityS2CPacket.RotateAndMoveRelative.class,
        EntitySetHeadYawS2CPacket.class, PlayerMoveC2SPacket.LookAndOnGround.class, EntityVelocityUpdateS2CPacket.class,
        PlayerMoveC2SPacket.PositionAndOnGround.class, PlayerMoveC2SPacket.Full.class, EntityPositionS2CPacket.class
    );

    public boolean isIgnored() {
        return Gadget.CONFIG.noFrequentPackets() && FREQUENT_PACKET_TYPES.contains(packet.getClass());
    }

    public int color() {
        return switch (state) {
            case PLAY -> 0xFF00FF00;
            case HANDSHAKING -> 0xFF808080;
            case LOGIN -> 0xFFFF0000;
            case STATUS -> 0xFFFFFF00;
        };
    }

    public String searchText() {
        StringBuilder search = new StringBuilder();

        search.append(ReflectionUtil.nameWithoutPackage(packet.getClass()));

        if (channelId != null)
            search.append(" ").append(channelId);

        return search.toString();
    }
}
