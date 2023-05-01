package io.wispforest.gadget.util;

import java.io.PrintStream;

public record FormattedDumper(PrintStream out) {
    public void write(int indent, String text) {
        for (int i = 0; i < indent; i++) out.write(' ');
        out.println(text);
    }

    public void writeLines(int indent, String text) {
        text.lines().forEach(line -> write(indent, line));
    }

    public void writeHexDump(int indent, byte[] bytes) {
        int index = 0;
        while (index < bytes.length) {
            StringBuilder line = new StringBuilder();

            line.append(String.format("%04x  ", index));

            int i;
            for (i = 0; i < 16 && index < bytes.length; i++) {
                short b = (short) (bytes[index] & 0xff);

                line.append(String.format("%02x ", b));
                index++;
            }

            line.append("   ".repeat(Math.max(0, 16 - i)));

            for (int j = 0; j < i; j++) {
                short b = (short) (bytes[index - i + j] & 0xff);

                if (b >= 32 && b < 127)
                    line.append((char) b);
                else
                    line.append('.');
            }

            write(indent, line.toString());
        }
    }
}
