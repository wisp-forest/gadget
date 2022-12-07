package io.wispforest.gadget.mappings;

public interface Mappings {
    String mapClass(String src);

    String mapField(String src);

    String unmapFieldId(String dst);

}
