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
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class QuiltMappings extends LoadingMappings {
    private static final String QM_API_ENTRYPOINT = "https://meta.quiltmc.org/v3/versions/quilt-mappings/" + SharedConstants.getGameVersion().getId();

    @Override
    protected void load(ProgressToast toast, MappingVisitor visitor) {
        try {
            Path mappingsDir = FabricLoader.getInstance().getGameDir().resolve("gadget").resolve("mappings");

            Files.createDirectories(mappingsDir);

            Path qmPath = mappingsDir.resolve("qm-" + SharedConstants.getGameVersion().getId() + ".tiny");

            if (Files.exists(qmPath)) {
                try (BufferedReader br = Files.newBufferedReader(qmPath)) {
                    Tiny2Reader.read(br, visitor);
                    return;
                }
            }

            toast.step(Text.translatable("message.gadget.progress.downloading_qm_versions"));

            QMVersion[] versions = DownloadUtil.read(toast, QM_API_ENTRYPOINT, QMVersion[].class);

            if (versions.length == 0) {
                throw new IllegalStateException("we malden");
            }

            int latestBuild = -1;
            String latestVersion = "";

            for (QMVersion version : versions) {
                if (version.build > latestBuild) {
                    latestVersion = version.version;
                    latestBuild = version.build;
                }
            }

            MemoryMappingTree tree = new MemoryMappingTree();

            IntermediaryLoader.loadIntermediary(toast, tree);

            var url = new URL(
                "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-mappings/"
                    + latestVersion
                    + "/quilt-mappings-"
                    + latestVersion
                    + "-tiny.gz");

            toast.step(Text.translatable("message.gadget.progress.downloading_qm"));
            try (var is = toast.loadWithProgress(url);
                 var gz = new GZIPInputStream(is)) {
                Tiny2Reader.read(new InputStreamReader(gz), tree);
            }

            try (var bw = Files.newBufferedWriter(qmPath)) {
                tree.accept(new Tiny2Writer(bw, false));
            }

            tree.accept(visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class QMVersion {
        private int build;
        private String version;
    }
}
