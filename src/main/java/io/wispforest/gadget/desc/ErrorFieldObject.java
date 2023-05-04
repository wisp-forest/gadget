package io.wispforest.gadget.desc;

import io.wispforest.gadget.util.ThrowableUtil;

public record ErrorFieldObject(String exceptionClass, String fullExceptionText) implements FieldObject {
    public static ErrorFieldObject fromException(Exception e) {
        return new ErrorFieldObject(e.getClass().getName(), ThrowableUtil.throwableToString(e));
    }

    @Override
    public String type() {
        return "error";
    }

    @Override
    public int color() {
        return 0xFF0000;
    }
}
