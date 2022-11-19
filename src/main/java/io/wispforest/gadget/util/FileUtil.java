package io.wispforest.gadget.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class FileUtil {
    // uses https://stackoverflow.com/a/25308216.
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private FileUtil() {

    }
    static {
        SIZE_FORMAT.setMaximumFractionDigits(2);
    }

    public static String formatDouble(double num) {
        return SIZE_FORMAT.format(num);
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

    public static List<Path> listSortedByFileName(Path directory) {
        try (var strem = Files.list(directory)) {
            return strem
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
