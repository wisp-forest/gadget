package me.basiqueevangelist.gadget.client.dump;

import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.util.Identifier;

public record DumpedPacket(boolean outbound, NetworkState state, Packet<?> packet, Identifier channelId) {
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
