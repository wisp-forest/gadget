package io.wispforest.gadget.decompile;

public interface QuiltflowerHandler {
    String mapClass(String name);

    String unmapClass(String name);

    byte[] getClassBytes(String name);

    String decompileClass(Class<?> klass);
}
