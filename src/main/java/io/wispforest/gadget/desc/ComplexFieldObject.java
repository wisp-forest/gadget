package io.wispforest.gadget.desc;

import io.wispforest.gadget.mappings.MappingsManager;

public record ComplexFieldObject(String className, String tag, boolean isRepeat) implements FieldObject {
    public String text() {
        return MappingsManager.displayMappings().mapClass(className) + tag;
    }

    @Override
    public String type() {
        return "complex";
    }

    @Override
    public int color() {
        return 0x0000FF;
    }
}
