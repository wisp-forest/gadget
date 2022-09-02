package me.basiqueevangelist.gadget.desc;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public record ErrorFieldObject(String exceptionClass, String fullExceptionText) implements FieldObject {
    public static ErrorFieldObject fromException(Exception e) {
        String exceptionMessage = e.getClass().getName();

        CharArrayWriter writer = new CharArrayWriter();
        e.printStackTrace(new PrintWriter(writer));
        String fullExceptionText = writer.toString();

        return new ErrorFieldObject(exceptionMessage, fullExceptionText);
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
