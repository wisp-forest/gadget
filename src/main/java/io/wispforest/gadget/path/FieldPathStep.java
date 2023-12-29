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
        if (!fieldName.startsWith("field_"))
            return fieldName;

        return LocalMappings.INSTANCE.mapField(fieldName);
    }

    public String fieldId() {
        return className + "#" + fieldName;
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
        if (!fieldName.startsWith("field_"))
            return fieldName;

        return MappingsManager.displayMappings().mapField(fieldName);
    }

    public static String remapFieldId(String id) {
        if (!id.contains("#"))
            return id;

        int hashIdx = id.lastIndexOf('#');
        String klass = id.substring(0, hashIdx);
        String field = id.substring(hashIdx + 1);

        return
            MappingsManager.displayMappings().mapClass(klass)
            + "#"
            + MappingsManager.displayMappings().mapField(field);
    }
}
