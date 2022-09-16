package io.wispforest.gadget.network;

import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.path.ObjectPath;

public record SetPrimitiveC2SPacket(InspectionTarget target, ObjectPath path, PrimitiveEditData data) {
}
