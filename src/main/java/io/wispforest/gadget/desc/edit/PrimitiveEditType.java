package io.wispforest.gadget.desc.edit;

public interface PrimitiveEditType<T> {
    T fromPacket(String repr);
    String toPacket(T value);
}