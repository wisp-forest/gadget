package io.wispforest.gadget.network;

import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class FabricPacketHacks {
    private static final Map<Identifier, PacketType<?>> TYPES = new HashMap<>();

    public static PacketType<?> getForId(Identifier id) {
        return TYPES.get(id);
    }

    @ApiStatus.Internal
    public static void saveType(PacketType<?> type) {
        TYPES.put(type.getId(), type);
    }
}
