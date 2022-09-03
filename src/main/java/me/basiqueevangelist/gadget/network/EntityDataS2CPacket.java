package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;

import java.util.Map;

public record EntityDataS2CPacket(int networkId, Map<ObjectPath, FieldData> fields) {
}
