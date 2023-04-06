package io.wispforest.gadget.network.packet.s2c;

import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.PathStep;

import java.util.Map;

public record OpenFieldDataScreenS2CPacket(InspectionTarget target, FieldData rootData, Map<PathStep, FieldData> rootChildren) {
}
