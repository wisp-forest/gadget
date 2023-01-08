package io.wispforest.gadget.mappings;

import net.fabricmc.mappingio.MappingVisitor;

import java.io.IOException;

public interface Mappings {
    String mapClass(String src);

    String mapField(String src);

    String unmapClass(String dst);

    String unmapFieldId(String dst);

    void load(MappingVisitor visitor) throws IOException;
}
