package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.GadgetConfigModel;
import net.minecraft.client.gui.screen.Screen;

public final class ComponentEventCounter {
    private static final ThreadLocal<ComponentEventCounter> STORE = ThreadLocal.withInitial(ComponentEventCounter::new);

    private int mutations = 0;
    private int componentInflations = 0;
    private long startNanos = 0;

    private ComponentEventCounter() {

    }

    public static void reset() {
        var counter = STORE.get();

        counter.componentInflations = 0;
        counter.mutations = 0;
        counter.startNanos = System.nanoTime();
    }

    public static void tally() {
        var mode = Gadget.CONFIG.uiCounterMode();

        if (mode == GadgetConfigModel.UICounterMode.OFF) return;

        var counter = STORE.get();

        if (mode == GadgetConfigModel.UICounterMode.LOG_ON_LONG_UPDATE) {
            long time = System.nanoTime() - counter.startNanos;

            if (time < 500000000) {
                return;
            }
        }

        if (counter.componentInflations > 0) {
            Gadget.LOGGER.info("{} mutations caused {} inflations", counter.mutations, counter.componentInflations);
        }
    }

    public static void countInflation() {
        STORE.get().componentInflations++;
    }

    public static void countMutation() {
        if (Screen.hasShiftDown()) {
            new Throwable("bro it's a mutation on " + Thread.currentThread().getName()).printStackTrace();
        }

        STORE.get().mutations++;
    }
}
