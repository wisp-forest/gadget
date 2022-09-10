package me.basiqueevangelist.gadget.path;

import com.google.common.collect.Iterators;

import java.lang.reflect.Array;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    @Override
    public void set(Object o, Object to) {
        if (o.getClass().isArray()) {
            Array.set(o, idx, to);
        } else if (o instanceof List<?> list) {
            ((List<Object>) list).set(idx, to);
        } else {
            throw new UnsupportedOperationException("Tried to set an IndexPathStep on a non-list object!");
        }
    }

    @Override
    public String toString() {
        return idx + "";
    }
}
