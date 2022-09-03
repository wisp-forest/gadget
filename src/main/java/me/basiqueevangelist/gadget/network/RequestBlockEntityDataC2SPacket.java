package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.util.math.BlockPos;

public record RequestBlockEntityDataC2SPacket(BlockPos pos, ObjectPath path) {
}
