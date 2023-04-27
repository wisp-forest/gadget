package io.wispforest.gadget.util;

public interface ErrorSink {
    /**
     * Accepts and stows away the provided {@link Throwable}.
     */
    void accept(Throwable throwable);
}
