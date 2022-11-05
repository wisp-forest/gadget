package io.wispforest.gadget.network.packet.s2c;

import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.ObjectPath;

import java.util.Map;

public record DataS2CPacket(InspectionTarget target, Map<ObjectPath, FieldData> fields) {
}
