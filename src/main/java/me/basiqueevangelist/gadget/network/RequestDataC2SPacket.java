package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;

public record RequestDataC2SPacket(InspectionTarget target, ObjectPath path) {
}
