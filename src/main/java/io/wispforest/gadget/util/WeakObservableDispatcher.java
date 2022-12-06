package io.wispforest.gadget.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class WeakObservableDispatcher<T> {
    private final List<WeakReference<Predicate<T>>> handlers = new ArrayList<>();

    public void register(Predicate<T> handler) {
        handlers.add(new WeakReference<>(handler));
    }

    public void handle(T value) {
        handlers.removeIf(x -> {
            var handler = x.get();

            return handler == null || handler.test(value);
        });
    }
}
