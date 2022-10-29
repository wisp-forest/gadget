package io.wispforest.gadget.path;

import io.wispforest.gadget.mappings.LocalMappings;
import io.wispforest.gadget.mappings.MappingsManager;
import io.wispforest.gadget.util.ReflectionUtil;

import java.lang.reflect.Field;

public record FieldPathStep(String fieldName) implements PathStep {
    public static FieldPathStep forField(Field field) {
        return new FieldPathStep(MappingsManager.unmapField(field));
    }

    public String runtimeName() {
        return LocalMappings.INSTANCE.mapField(fieldName);
    }

    @Override
    public Object follow(Object o) {
        try {
            var field = ReflectionUtil.findField(o.getClass(), runtimeName());

            if (!field.canAccess(o))
                field.setAccessible(true);

            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object o, Object to) {
        try {
            var field = ReflectionUtil.findField(o.getClass(), runtimeName());

            if (!field.canAccess(o))
                field.setAccessible(true);

            field.set(o, to);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return MappingsManager.displayMappings().mapField(fieldName);
    }
}
