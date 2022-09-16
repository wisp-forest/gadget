package io.wispforest.gadget.network;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public record BlockEntityTarget(BlockPos pos) implements InspectionTarget {
    @Override
    public @Nullable Object resolve(World w) {
        return w.getBlockEntity(pos);
    }
}
