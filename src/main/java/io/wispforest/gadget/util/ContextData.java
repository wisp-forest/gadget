package io.wispforest.gadget.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class ContextData<S extends ContextData<S>> {
    private final Map<Key<S, ?>, Object> entries = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(Key<S, T> key) {
        T value = (T) entries.get(key);

        if (value == null) {
            value = key.factory.apply((S) this);
            entries.put(key, value);
        }

        return value;
    }

    public static final class Key<S extends ContextData<S>, T> {
        private final Function<S, T> factory;

        public Key(Function<S, T> factory) {
            this.factory = factory;
        }
    }
}
