package io.wispforest.gadget.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class CancellationToken {
    public static final CancellationToken NONE = new CancellationToken();

    private final WeakReference<CancellationTokenSource> sourceRef;
    boolean cancelled = false;

    CancellationToken(CancellationTokenSource source) {
        this.sourceRef = new WeakReference<>(source);
    }

    private CancellationToken() {
        this.sourceRef = new WeakReference<>(null);
        this.cancelled = false;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public void throwIfCancelled() {
        if (cancelled())
            throw new CancellationException();
    }

    public <T> CompletableFuture<T> wrapFuture(CompletableFuture<T> original) {
        if (!original.isDone()) {
            var subscription = register(() -> {
                if (cancelled()) original.cancel(true);
            });

            original.whenComplete((unused1, unused2) -> {
                subscription.close();
            });
        }

        return original;
    }

    public Subscription register(Runnable action) {
        if (cancelled) {
            action.run();
            return Subscription.EMPTY;
        }

        CancellationTokenSource source = sourceRef.get();

        if (source == null) return Subscription.EMPTY;

        synchronized (source.lock) {
            if (source.listeners == null) {
                action.run();
                return Subscription.EMPTY;
            }

            source.listeners.add(action);
            return new Subscription(this, action);
        }
    }

    public static class Subscription implements InfallibleClosable {
        private static final Subscription EMPTY = new Subscription(null, null);
        private CancellationToken token;
        private Runnable runnable;

        public Subscription(CancellationToken token, Runnable runnable) {
            this.token = token;
            this.runnable = runnable;
        }

        @Override
        public void close() {
            if (token != null) {
                if (token.cancelled()) {
                    token = null;
                    runnable = null;
                    return;
                }

                var source = token.sourceRef.get();

                if (source == null) {
                    token = null;
                    runnable = null;
                    return;
                }

                synchronized (source.lock) {
                    if (source.listeners == null) {
                        token = null;
                        runnable = null;
                        return;
                    }

                    source.listeners.remove(runnable);
                    token = null;
                    runnable = null;
                }
            }
        }
    }
}
