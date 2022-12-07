package io.wispforest.gadget.path;

import io.wispforest.gadget.mappings.LocalMappings;
import io.wispforest.gadget.mappings.MappingsManager;
import net.auoeke.reflect.Accessor;

import java.lang.reflect.Field;

public record FieldPathStep(String className, String fieldName) implements PathStep {
    public static FieldPathStep forField(Field field) {
        return new FieldPathStep(MappingsManager.unmapClass(field.getDeclaringClass()), MappingsManager.unmapField(field));
    }

    public String runtimeName() {
        return LocalMappings.INSTANCE.mapField(fieldName);
    }

    public String fieldId() {
        return MappingsManager.displayMappings().mapClass(className)
            + "#"
            + MappingsManager.displayMappings().mapField(fieldName);
    }

    @Override
    public Object follow(Object o) {
        return Accessor.get(o, runtimeName());
    }

    @Override
    public void set(Object o, Object to) {
        Accessor.put(o, runtimeName(), to);
    }

    @Override
    public String toString() {
        return MappingsManager.displayMappings().mapField(fieldName);
    }
}
