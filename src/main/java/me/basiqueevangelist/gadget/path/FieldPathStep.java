package me.basiqueevangelist.gadget.path;

import me.basiqueevangelist.gadget.util.ReflectionUtil;

public record FieldPathStep(String fieldName) implements PathStep {
    @Override
    public Object follow(Object o) {
        try {
            var field = ReflectionUtil.findField(o.getClass(), fieldName);

            if (!field.canAccess(o))
                field.setAccessible(true);

            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
