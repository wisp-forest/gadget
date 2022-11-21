package io.wispforest.gadget.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumberUtil {
    // uses https://stackoverflow.com/a/25308216.
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ROOT));

    static {
        NumberUtil.SIZE_FORMAT.setMaximumFractionDigits(2);
    }

    public static String formatDouble(double num) {
        return SIZE_FORMAT.format(num);
    }

    public static String formatPercent(double num) {
        return SIZE_FORMAT.format(num * 100) + "%";
    }

    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return SIZE_FORMAT.format((double) size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return SIZE_FORMAT.format((double) size / 1024 / 1024) + " MB";
        } else {
            return SIZE_FORMAT.format((double) size / 1024 / 1024 / 1024) + " GB";
        }
    }
}
