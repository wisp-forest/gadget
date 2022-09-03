package me.basiqueevangelist.gadget.network;

import me.basiqueevangelist.gadget.path.ObjectPath;

public record RequestEntityDataC2SPacket(int networkId, ObjectPath path) {
}
