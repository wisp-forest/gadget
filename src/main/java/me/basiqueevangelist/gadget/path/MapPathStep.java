package me.basiqueevangelist.gadget.path;

import me.basiqueevangelist.gadget.util.PrettyPrinters;

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

    @Override
    public String toString() {
        return "[" + PrettyPrinters.tryPrint(type.fromImpl().apply(key)) + "]";
    }
}
