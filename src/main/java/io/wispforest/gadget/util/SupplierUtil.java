package io.wispforest.gadget.util;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;

public final class SupplierUtil {
    private SupplierUtil() {

    }

    public static <T> Supplier<T> weakLazy(Supplier<T> wrapped) {
        if (wrapped instanceof WeakLazy<T> lazy)
            return lazy;

        return new WeakLazy<>(wrapped);
    }

    private static class WeakLazy<T> implements Supplier<T> {
        private WeakReference<T> holder = null;
        private final Supplier<T> wrapped;

        public WeakLazy(Supplier<T> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public T get() {
            T value = holder == null ? null : holder.get();
            if (value == null) {
                value = wrapped.get();
                holder = new WeakReference<>(value);
            }

            return value;
        }
    }
}
