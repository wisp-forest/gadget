package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.desc.edit.PrimitiveEditData;
import me.basiqueevangelist.gadget.path.ObjectPath;

public record SetPrimitiveC2SPacket(InspectionTarget target, ObjectPath path, PrimitiveEditData data) {
}
