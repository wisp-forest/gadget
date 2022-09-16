package io.wispforest.gadget.path;

import io.wispforest.gadget.util.PrettyPrinters;

import java.util.Map;

public record MapPathStep(MapPathStepType type, String key) implements PathStep {
    @SuppressWarnings("unchecked")
    @Override
    public Object follow(Object o) {
        if (!(o instanceof Map<?, ?> map))
            throw new UnsupportedOperationException("Tried to use MapPathStep on non-map!");

        var basedMap = (Map<Object, ?>) map;

        return basedMap.get(type.fromImpl().apply(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object o, Object to) {
        if (!(o instanceof Map<?, ?> map))
            throw new UnsupportedOperationException("Tried to use MapPathStep on non-map!");

        var basedMap = (Map<Object, Object>) map;

        basedMap.put(type.fromImpl().apply(key), to);
    }

    @Override
    public String toString() {
        return "[" + PrettyPrinters.tryPrint(type.fromImpl().apply(key)) + "]";
    }
}
