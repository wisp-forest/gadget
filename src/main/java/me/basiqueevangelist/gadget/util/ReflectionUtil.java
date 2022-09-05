package me.basiqueevangelist.gadget.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        if (klass == Object.class)
            return List.of(klass.getDeclaredFields());

        return Iterables.concat(List.of(klass.getDeclaredFields()), allFields(klass.getSuperclass()));
    }
}
