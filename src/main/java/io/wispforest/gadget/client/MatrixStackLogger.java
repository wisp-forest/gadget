package io.wispforest.gadget.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ReflectionUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public final class MatrixStackLogger {
    private static volatile int ERROR_MODE = 0;
    private static final Map<MatrixStack, StringBuilder> LOGS = Collections.synchronizedMap(new WeakHashMap<>());

    private MatrixStackLogger() {

    }

    public static void logOp(MatrixStack stack, boolean push, int indent) {
        if (ERROR_MODE != 2 && !(Gadget.CONFIG.internalSettings.debugMatrixStackDebugging() && Screen.hasShiftDown())) return;

        var log = LOGS.computeIfAbsent(stack, unused -> new StringBuilder());

        log.append(" ".repeat(indent));
        log.append(push ? "> " : "< ").append(ReflectionUtil.getCallingMethodData(4));
        log.append("\n");
    }

    public static boolean tripError(MatrixStack stack, String message) {
        if (!Gadget.CONFIG.matrixStackDebugging()) return false;

        switch (ERROR_MODE) {
            case 0 -> ERROR_MODE = 1;
            case 2 -> {
                Gadget.LOGGER.error("Push/pop log of matrix stack:\n{}", LOGS.getOrDefault(stack, new StringBuilder()));
                throw new IllegalStateException(message);
            }
        }

        return true;
    }

    public static void startLoggingIfNeeded() {
        if (Gadget.CONFIG.internalSettings.debugMatrixStackDebugging() && Screen.hasShiftDown()) {
            for (var log : LOGS.values()) {
                Gadget.LOGGER.error("log:\n{}", log);
            }
        }

        if (ERROR_MODE == 1)
            ERROR_MODE = 2;

        LOGS.clear();
    }
}
