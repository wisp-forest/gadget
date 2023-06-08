package io.wispforest.gadget.desc;

import io.wispforest.gadget.mappings.MappingsManager;

public record BytesFieldObject(String bufferClass, byte[] data) implements FieldObject {
    public String text() {
        String mappedBufferClass = MappingsManager.displayMappings().mapClass(bufferClass);
        return mappedBufferClass.substring(mappedBufferClass.lastIndexOf('.') + 1)
            + ", "
            + data.length
            + " byte"
            + (data.length != 1 ? "s" : "");
    }

    @Override
    public String type() {
        return "bytes";
    }

    @Override
    public int color() {
        return 0x00FF00;
    }
}