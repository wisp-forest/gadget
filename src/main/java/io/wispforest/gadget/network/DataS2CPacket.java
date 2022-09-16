package io.wispforest.gadget.network;

import io.wispforest.gadget.path.ObjectPath;

import java.util.Map;

public record DataS2CPacket(InspectionTarget target, Map<ObjectPath, FieldData> fields) {
}
