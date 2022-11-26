package io.wispforest.gadget.pond;

public class MixinState {
    public static final ThreadLocal<Boolean> IS_IGNORING_ERRORS = new ThreadLocal<>();
}
