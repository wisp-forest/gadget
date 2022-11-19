package io.wispforest.gadget.path;

import io.wispforest.owo.network.serialization.PacketBufSerializer;

public record EnumMapPathStepType(Class<?> klass) implements MapPathStepType {
    public static void init() {
        PacketBufSerializer.register(EnumMapPathStepType.class, new PacketBufSerializer<>(
            (buf, type) -> buf.writeString(type.klass.getName()),
            buf -> {
                try {
                    return new EnumMapPathStepType(Class.forName(buf.readString()));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object fromNetwork(String data) {
        return Enum.valueOf((Class) klass, data);
    }

    @Override
    public String toNetwork(Object obj) {
        return ((Enum<?>) obj).name();
    }

    @Override
    public String toPretty(Object obj) {
        return ((Enum<?>) obj).name();
    }
}
