package me.basiqueevangelist.gadget.util;

import org.spongepowered.include.com.google.common.collect.Iterables;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public final class ReflectionUtil {
    private ReflectionUtil() {

    }

    public static <T> T findFor(Class<?> klass, Map<Class<?>, T> map) {
        T val = map.get(klass);

        if (val != null)
            return val;

        if (klass == Object.class)
            return null;

        return findFor(klass.getSuperclass(), map);
    }

    public static Field findField(Class<?> klass, String name) {
        try {
            return klass.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (klass == Object.class)
                return null;

            return findField(klass.getSuperclass(), name);
        }
    }

    public static Iterable<Field> allFields(Class<?> klass) {
        if (klass == Object.class)
            return List.of(klass.getDeclaredFields());

        return Iterables.concat(List.of(klass.getDeclaredFields()), allFields(klass.getSuperclass()));
    }
}
