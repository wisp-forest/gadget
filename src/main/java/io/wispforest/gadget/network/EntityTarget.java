package io.wispforest.gadget.network;

import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record EntityTarget(int networkId) implements InspectionTarget {
    @Override
    public @Nullable Object resolve(World w) {
        return w.getEntityById(networkId);
    }
}
