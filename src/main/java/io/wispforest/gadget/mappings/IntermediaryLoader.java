package io.wispforest.gadget.mappings;

import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public final class IntermediaryLoader {
    public static final String INTERMEDIARY_ENDPOINT
        = "https://maven.fabricmc.net/net/fabricmc/intermediary/"
        + SharedConstants.getGameVersion().getName()
        + "/intermediary-"
        + SharedConstants.getGameVersion().getName()
        + "-v2.jar";

    private IntermediaryLoader() {

    }

    public static void loadIntermediary(ProgressToast toast, MappingVisitor visitor) throws IOException {
        Path mappingsDir = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("mappings");

        Files.createDirectories(mappingsDir);

        Path intermediaryPath = mappingsDir.resolve("intermediary-" + SharedConstants.getGameVersion().getName() + ".jar");

        if (!Files.exists(intermediaryPath)) {
            toast.step(Text.translatable("message.gadget.progress.downloading_intermediary"));

            try (var is = toast.loadWithProgress(new URL(INTERMEDIARY_ENDPOINT))) {
                FileUtils.copyToFile(is, intermediaryPath.toFile());
            }
        }

        try (FileSystem fs = FileSystems.newFileSystem(intermediaryPath, (ClassLoader) null);
             BufferedReader br = Files.newBufferedReader(fs.getPath("mappings/mappings.tiny"))) {
            Tiny2Reader.read(br, visitor);
        }
    }
}
