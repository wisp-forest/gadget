package io.wispforest.gadget.network.packet.s2c;

import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;

import java.util.Map;

public record FieldDataResponseS2CPacket(InspectionTarget target, ObjectPath path, Map<PathStep, FieldData> fields) {
}
