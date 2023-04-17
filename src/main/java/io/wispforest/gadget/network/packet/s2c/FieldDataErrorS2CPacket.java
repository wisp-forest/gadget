package io.wispforest.gadget.network.packet.s2c;

import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.path.ObjectPath;
import net.minecraft.text.Text;

public record FieldDataErrorS2CPacket(InspectionTarget target, ObjectPath path, Text message) {
}
