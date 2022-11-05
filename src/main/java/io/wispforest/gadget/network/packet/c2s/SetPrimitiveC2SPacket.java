package io.wispforest.gadget.network.packet.c2s;

import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.ObjectPath;

public record SetPrimitiveC2SPacket(InspectionTarget target, ObjectPath path, PrimitiveEditData data) {
}
