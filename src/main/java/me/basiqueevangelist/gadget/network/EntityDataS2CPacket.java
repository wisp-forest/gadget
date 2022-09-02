package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.util.FieldPath;

import java.util.Map;

public record EntityDataS2CPacket(int networkId, Map<FieldPath, FieldData> fields) {
}
