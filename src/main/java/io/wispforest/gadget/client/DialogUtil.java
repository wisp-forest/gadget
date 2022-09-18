package io.wispforest.gadget.client;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.util.List;

public final class DialogUtil {
    private DialogUtil() {

    }

    public static String openFileDialog(String title, @Nullable String defaultPath, @Nullable List<String> patterns, @Nullable String filterDesc, boolean allowMultipleSelections) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer patternsBuf = null;

            if (patterns != null) {
                patternsBuf = stack.mallocPointer(patterns.size());

                for (int i = 0; i < patterns.size(); i++) {
                    stack.nUTF8Safe(patterns.get(i), true);
                    patternsBuf.put(i, stack.getPointerAddress());
                }
            }

            return TinyFileDialogs.tinyfd_openFileDialog(title, defaultPath, patternsBuf, filterDesc, allowMultipleSelections);
        }
    }
}
