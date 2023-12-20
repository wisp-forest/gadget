package io.wispforest.gadget.shell;

import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class ConsumerPrintStream extends PrintStream {
    private static final ErroringStream STREAM = new ErroringStream();
    private final Consumer<String> lineConsumer;

    public ConsumerPrintStream(Consumer<String> lineConsumer) {
        super(STREAM);
        this.lineConsumer = lineConsumer;
    }

    public void println(@Nullable String message) {
        lineConsumer.accept(message);
    }

    public void println(Object object) {
        lineConsumer.accept(String.valueOf(object));
    }

    private static class ErroringStream extends OutputStream {
        @Override
        public void write(int b) {
            throw new IllegalStateException("Bypassed ConsumerPrintStream");
        }
    }
}
