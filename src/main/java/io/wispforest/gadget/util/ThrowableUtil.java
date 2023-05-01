package io.wispforest.gadget.util;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public final class ThrowableUtil {
    private ThrowableUtil() {

    }

    public static String throwableToString(Throwable t) {
        CharArrayWriter writer = new CharArrayWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString().replace("\t", "    ");
    }
}
