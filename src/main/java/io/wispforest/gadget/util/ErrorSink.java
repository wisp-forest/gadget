package io.wispforest.gadget.util;

public interface ErrorSink {
    ErrorSink NULL = t -> {};

    /**
     * Accepts and stows away the provided {@link Throwable}.
     */
    void accept(Throwable throwable);
}
