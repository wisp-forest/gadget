// Portions of this code are copied from NEC.
//
// Copyright (c) 2021 Fudge and NEC contributors
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package io.wispforest.gadget.mappings;

import io.wispforest.gadget.util.DownloadUtil;
import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class YarnMappings implements Mappings {
    private static final String YARN_API_ENTRYPOINT = "https://meta.fabricmc.net/v2/versions/yarn/" + SharedConstants.getGameVersion().getId();

    private volatile Map<String, String> intermediaryToFieldMap = Collections.emptyMap();
    private volatile Map<String, String> intermediaryToClassMap = Collections.emptyMap();
    private volatile Map<String, String> fieldIdToIntermediaryMap = Collections.emptyMap();

    public YarnMappings() {
        ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_mappings"));
        toast.follow(CompletableFuture.runAsync(() -> {
            var tree = load(toast);

            var classMap = new HashMap<String, String>();
            var fieldMap = new HashMap<String, String>();

            for (var def : tree.getClasses()) {
                classMap.put(def.getName("intermediary"), def.getName("named"));

                for (var field : def.getFields()) {
                    fieldMap.put(field.getName("intermediary"), field.getName("named"));
                }
            }

            intermediaryToFieldMap = fieldMap;
            intermediaryToClassMap = classMap;
            fieldIdToIntermediaryMap = MappingUtils.createFieldIdUnmap(tree, "named");
        }), false);
    }

    private MappingTree load(ProgressToast toast) {
        try {
            Path mappingsDir = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("mappings");

            Files.createDirectories(mappingsDir);

            Path yarnPath = mappingsDir.resolve("yarn-" + SharedConstants.getGameVersion().getId() + ".jar");

            if (!Files.exists(yarnPath)) {
                toast.step(Text.translatable("message.gadget.progress.downloading_yarn_versions"));
                YarnVersion[] versions = DownloadUtil.read(toast, YARN_API_ENTRYPOINT, YarnVersion[].class);

                if (versions.length == 0) {
                    throw new IllegalStateException("we malden");
                }

                int latestBuild = -1;
                String latestVersion = "";

                for (YarnVersion version : versions) {
                    if (version.build > latestBuild) {
                        latestVersion = version.version;
                        latestBuild = version.build;
                    }
                }

                toast.step(Text.translatable("message.gadget.progress.downloading_yarn"));
                try (var is = toast.loadWithProgress(new URL("https://maven.fabricmc.net/net/fabricmc/yarn/" + latestVersion + "/yarn-" + latestVersion + "-v2.jar"))) {
                    FileUtils.copyInputStreamToFile(is, yarnPath.toFile());
                }
            }

            try (FileSystem fs = FileSystems.newFileSystem(yarnPath, (ClassLoader) null)) {
                try (var br = Files.newBufferedReader(fs.getPath("mappings/mappings.tiny"))) {
                    MemoryMappingTree tree = new MemoryMappingTree();
                    Tiny2Reader.read(br, tree);
                    return tree;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String mapClass(String src) {
        src = src.replace('.', '/');

        return intermediaryToClassMap.getOrDefault(src, src).replace('/', '.');
    }

    @Override
    public String mapField(String src) {
        return intermediaryToFieldMap.getOrDefault(src, src);
    }

    @Override
    public String unmapFieldId(String dst) {
        return fieldIdToIntermediaryMap.getOrDefault(dst, dst);
    }

    private static class YarnVersion {
        private int build;
        private String version;
    }
}
