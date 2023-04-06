package io.wispforest.gadget.network.packet.c2s;

import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.ObjectPath;

public record FieldDataRequestC2SPacket(InspectionTarget target, ObjectPath path, int from, int limit) {
}
