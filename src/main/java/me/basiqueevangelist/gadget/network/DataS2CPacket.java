package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;

import java.util.Map;

public record DataS2CPacket(InspectionTarget target, Map<ObjectPath, FieldData> fields) {
}
