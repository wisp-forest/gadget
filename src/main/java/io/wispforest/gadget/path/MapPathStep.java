package io.wispforest.gadget.path;

import java.util.Map;

public record MapPathStep(MapPathStepType type, String key) implements PathStep {
    @SuppressWarnings("unchecked")
    @Override
    public Object follow(Object o) {
        if (!(o instanceof Map<?, ?> map))
            throw new UnsupportedOperationException("Tried to use MapPathStep on non-map!");

        var basedMap = (Map<Object, ?>) map;

        return basedMap.get(type.fromNetwork(key));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object o, Object to) {
        if (!(o instanceof Map<?, ?> map))
            throw new UnsupportedOperationException("Tried to use MapPathStep on non-map!");

        var basedMap = (Map<Object, Object>) map;

        basedMap.put(type.fromNetwork(key), to);
    }

    @Override
    public String toString() {
        return "[" + type.toPretty(type.fromNetwork(key)) + "]";
    }
}
