package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.util.FieldPath;
import net.minecraft.util.math.BlockPos;

public record RequestBlockEntityDataC2SPacket(BlockPos pos, FieldPath path) {
}
