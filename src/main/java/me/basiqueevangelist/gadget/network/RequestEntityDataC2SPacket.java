package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.util.FieldPath;

public record RequestEntityDataC2SPacket(int networkId, FieldPath path) {
}
