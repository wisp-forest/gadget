package io.wispforest.gadget.util;

import java.util.ArrayList;
import java.util.List;

public class CancellationTokenSource {
    List<Runnable> listeners = new ArrayList<>();
    private final CancellationToken token = new CancellationToken(this);
    final Object lock = new Object(); // bruh

    public CancellationToken token() {
        return token;
    }

    public void cancel() {
        if (token.cancelled) return;

        synchronized (lock) {
            if (token.cancelled) return;

            token.cancelled = true;

            for (Runnable r : listeners) {
                r.run();
            }

            listeners.clear();
            listeners = null;
        }
    }
}
