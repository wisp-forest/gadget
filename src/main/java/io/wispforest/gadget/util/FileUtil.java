package io.wispforest.gadget.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class FileUtil {
    private FileUtil() {

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
