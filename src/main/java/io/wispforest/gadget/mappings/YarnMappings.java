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
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.minecraft.SharedConstants;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class YarnMappings implements Mappings {
    private static final String YARN_API_ENTRYPOINT = "https://meta.fabricmc.net/v2/versions/yarn/" + SharedConstants.getGameVersion().getName();

    private final Map<String, String> intermediaryToFieldMap = new HashMap<>();
    private final Map<String, String> intermediaryToClassMap = new HashMap<>();

    public YarnMappings() {
        var tree = load();

        for (var def : tree.getClasses()) {
            intermediaryToClassMap.put(def.getName("intermediary"), def.getName("named"));

            for (var field : def.getFields()) {
                intermediaryToFieldMap.put(field.getName("intermediary"), field.getName("named"));
            }
        }
    }

    private TinyTree load() {
        try {
            Path mappingsDir = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("mappings");

            Files.createDirectories(mappingsDir);

            Path yarnPath = mappingsDir.resolve("yarn-" + SharedConstants.getGameVersion().getName() + ".jar");

            if (!Files.exists(yarnPath)) {
                YarnVersion[] versions = DownloadUtil.read(YARN_API_ENTRYPOINT, YarnVersion[].class);

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

                FileUtils.copyURLToFile(new URL("https://maven.fabricmc.net/net/fabricmc/yarn/" + latestVersion + "/yarn-" + latestVersion + "-v2.jar"), yarnPath.toFile());
            }

            try (FileSystem fs = FileSystems.newFileSystem(yarnPath, (ClassLoader) null)) {
                try (var br = Files.newBufferedReader(fs.getPath("mappings/mappings.tiny"))) {
                    return TinyMappingFactory.load(br);
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

    private static class YarnVersion {
        private int build;
        private String version;
    }
}
