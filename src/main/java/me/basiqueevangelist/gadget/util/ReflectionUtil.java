package me.basiqueevangelist.gadget.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.util.*;

public final class ReflectionUtil {
    @SuppressWarnings("RedundantUnmodifiable")
    private static final Class<?> UNMODIFIABLE_MAP_CLASS = Collections.unmodifiableMap(Collections.emptyMap()).getClass();

    @SuppressWarnings("RedundantUnmodifiable")
    private static final Class<?> UNMODIFIABLE_LIST_CLASS = Collections.unmodifiableList(Collections.emptyList()).getClass();

    @SuppressWarnings("RedundantUnmodifiable")
    private static final Class<?> UNMODIFIABLE_SET_CLASS = Collections.unmodifiableSet(Collections.emptySet()).getClass();

    @SuppressWarnings("RedundantUnmodifiable")
    private static final Class<?> UNMODIFIABLE_COLLECTION_CLASS = Collections.unmodifiableCollection(Collections.emptyList()).getClass();

    private ReflectionUtil() {

    }

    public static boolean guessImmutability(Iterable<?> iterable) {
        return iterable instanceof ImmutableCollection<?>
            || iterable.getClass() == UNMODIFIABLE_LIST_CLASS
            || iterable.getClass() == UNMODIFIABLE_SET_CLASS
            || iterable.getClass() == UNMODIFIABLE_COLLECTION_CLASS;
    }

    public static boolean guessImmutability(Map<?, ?> map) {
        return map instanceof ImmutableMap<?,?> || map.getClass() == UNMODIFIABLE_MAP_CLASS;
    }

    public static String nameWithoutPackage(Class<?> klass) {
        String full = klass.getName();
        return full.substring(full.lastIndexOf('.') + 1);
    }

    public static String prettyName(Class<?> klass) {
        if (klass.isArray())
            return prettyName(klass.componentType()) + "[]";

        return klass.getName();
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
        TreeSet<Field> fields = new TreeSet<>(Comparator.comparing(Field::getName));

        while (klass != Object.class) {
            fields.addAll(List.of(klass.getDeclaredFields()));

            klass = klass.getSuperclass();
        }

        return fields;
    }
}
