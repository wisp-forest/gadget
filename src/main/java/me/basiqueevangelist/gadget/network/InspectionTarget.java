package me.basiqueevangelist.gadget.network;

import io.wispforest.owo.network.serialization.SealedPolymorphic;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SealedPolymorphic
public sealed interface InspectionTarget permits BlockEntityTarget, EntityTarget {
    @Nullable Object resolve(World w);
}
