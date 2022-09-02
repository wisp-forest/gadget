package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.util.FieldPath;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public record BlockEntityDataS2CPacket(BlockPos pos, Map<FieldPath, FieldData> fields) {
}
