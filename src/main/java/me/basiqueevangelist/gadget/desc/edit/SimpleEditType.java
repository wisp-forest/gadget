package me.basiqueevangelist.gadget.desc.edit;

import java.util.function.Function;

public class SimpleEditType<T> implements PrimitiveEditType<T> {
    private final Function<String, T> fromImpl;
    private final Function<T, String> toImpl;

    public SimpleEditType(Function<String, T> fromImpl, Function<T, String> toImpl) {
        this.fromImpl = fromImpl;
        this.toImpl = toImpl;
    }

    @Override
    public T fromPacket(String repr) {
        return fromImpl.apply(repr);
    }

    @Override
    public String toPacket(T value) {
        return toImpl.apply(value);
    }
}
