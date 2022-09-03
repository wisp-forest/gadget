package me.basiqueevangelist.gadget.path;

import com.google.common.collect.Iterators;

import java.lang.reflect.Array;

public record IndexPathStep(int idx) implements PathStep {
    @Override
    public Object follow(Object o) {
        if (o.getClass().isArray()) {
            return Array.get(o, idx);
        } else if (o instanceof Iterable<?> iter) {
            return Iterators.get(iter.iterator(), idx);
        } else {
            throw new UnsupportedOperationException("Tried to use an IndexPathStep on a non-iterable object!");
        }
    }

    @Override
    public String toString() {
        return idx + "";
    }
}
