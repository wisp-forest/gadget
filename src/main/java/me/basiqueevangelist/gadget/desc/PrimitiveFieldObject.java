package me.basiqueevangelist.gadget.desc;

import me.basiqueevangelist.gadget.desc.edit.PrimitiveEditData;

import java.util.Optional;

public record PrimitiveFieldObject(String contents, Optional<PrimitiveEditData> editData) implements FieldObject {
    @Override
    public String type() {
        return "primitive";
    }

    @Override
    public int color() {
        return 0xFFFF00;
    }
}
