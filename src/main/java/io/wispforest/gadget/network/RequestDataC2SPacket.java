package io.wispforest.gadget.network;

import io.wispforest.gadget.path.ObjectPath;

public record RequestDataC2SPacket(InspectionTarget target, ObjectPath path) {
}
