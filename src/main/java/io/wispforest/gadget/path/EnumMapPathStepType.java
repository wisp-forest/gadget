package io.wispforest.gadget.path;

import io.wispforest.owo.serialization.Endec;
import io.wispforest.owo.serialization.endec.ReflectiveEndecBuilder;

public record EnumMapPathStepType(Class<?> klass) implements MapPathStepType {
    public static final Endec<EnumMapPathStepType> ENDEC = Endec.STRING.xmap(
        name -> {
            try {
                return new EnumMapPathStepType(Class.forName(name));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        },
        type -> type.klass.getName()
    );

    public static void init() {
        ReflectiveEndecBuilder.register(ENDEC, EnumMapPathStepType.class);
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
