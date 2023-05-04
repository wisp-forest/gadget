package io.wispforest.gadget.util;

public interface InfallibleClosable extends AutoCloseable {
    @Override
    void close();
}
