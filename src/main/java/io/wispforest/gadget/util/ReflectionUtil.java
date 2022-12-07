package io.wispforest.gadget.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import io.wispforest.gadget.mappings.MappingsManager;

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
        String full = MappingsManager.remapClassToDisplay(klass);
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

    public static String getCallingMethodData(int depth) {
        return StackWalker.getInstance().walk(s -> s
            .skip(depth)
            .findFirst())
            .map(x -> x.getClassName() + "#" + x.getMethodName() + ":" + x.getLineNumber())
            .orElse("unknown");
    }
}
