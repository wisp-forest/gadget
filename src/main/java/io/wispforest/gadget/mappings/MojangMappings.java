package io.wispforest.gadget.mappings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.gadget.util.DownloadUtil;
import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MojangMappings implements Mappings {
    public static final String VERSION_MANIFEST_ENDPOINT = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private volatile Map<String, String> intermediaryToFieldMap = Collections.emptyMap();
    private volatile Map<String, String> intermediaryToClassMap = Collections.emptyMap();
    private volatile Map<String, String> fieldIdToIntermediaryMap = Collections.emptyMap();

    public MojangMappings() {
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

            Path mojPath = mappingsDir.resolve("mojmap-" + SharedConstants.getGameVersion().getName() + ".tiny");

            if (Files.exists(mojPath)) {
                try (BufferedReader br = Files.newBufferedReader(mojPath)) {
                    var tree = new MemoryMappingTree();

                    Tiny2Reader.read(br, tree);

                    return tree;
                }
            }

            MemoryMappingTree tree = new MemoryMappingTree(true);

            IntermediaryLoader.loadIntermediary(toast, tree);

            toast.step(Text.translatable("message.gadget.progress.downloading_minecraft_versions"));
            JsonArray versions = JsonHelper.getArray(DownloadUtil.read(toast, VERSION_MANIFEST_ENDPOINT), "versions");
            String chosenUrl = null;

            for (int i = 0; i < versions.size(); i++) {
                JsonObject version = versions.get(i).getAsJsonObject();

                if (JsonHelper.getString(version, "id").equals(SharedConstants.getGameVersion().getId()))
                    chosenUrl = JsonHelper.getString(version, "url");
            }

            if (chosenUrl == null)
                throw new UnsupportedOperationException("Couldn't find version " + SharedConstants.getGameVersion().getId() + " on Mojang's servers!");

            toast.step(Text.translatable("message.gadget.progress.downloading_minecraft_version_manifest"));
            JsonObject manifest = DownloadUtil.read(toast, chosenUrl);
            JsonObject downloads = JsonHelper.getObject(manifest, "downloads");
            JsonObject clientMappings = JsonHelper.getObject(downloads, "client_mappings");
            JsonObject serverMappings = JsonHelper.getObject(downloads, "server_mappings");
            var sw = new MappingSourceNsSwitch(tree, "official");

            toast.step(Text.translatable("message.gadget.progress.downloading_client_mappings"));
            readProGuardInto(toast, JsonHelper.getString(clientMappings, "url"), sw);
            toast.step(Text.translatable("message.gadget.progress.downloading_server_mappings"));
            readProGuardInto(toast, JsonHelper.getString(serverMappings, "url"), sw);

            try (BufferedWriter bw = Files.newBufferedWriter(mojPath)) {
                tree.accept(new Tiny2Writer(bw, false));
            }

            return tree;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readProGuardInto(ProgressToast toast, String url, MappingVisitor visitor) throws IOException {
        try (var is = toast.loadWithProgress(new URL(url))) {
            ProGuardReader.read(new InputStreamReader(new BufferedInputStream(is)), "named", "official", visitor);
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
}
