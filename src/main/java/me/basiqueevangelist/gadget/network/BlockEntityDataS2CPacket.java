package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

public record BlockEntityDataS2CPacket(BlockPos pos, Map<ObjectPath, FieldData> fields) {
}
