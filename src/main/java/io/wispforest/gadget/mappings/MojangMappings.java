package io.wispforest.gadget.mappings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.wispforest.gadget.util.DownloadUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.ProGuardReader;
import net.fabricmc.mappingio.format.Tiny2Reader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.SharedConstants;
import net.minecraft.util.JsonHelper;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MojangMappings implements Mappings {
    public static final String VERSION_MANIFEST_ENDPOINT = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";
    public static final String INTERMEDIARY_ENDPOINT
        = "https://maven.fabricmc.net/net/fabricmc/intermediary/"
        + SharedConstants.getGameVersion().getName()
        + "/intermediary-"
        + SharedConstants.getGameVersion().getName()
        + "-v2.jar";

    private final Map<String, String> intermediaryToFieldMap = new HashMap<>();
    private final Map<String, String> intermediaryToClassMap = new HashMap<>();

    public MojangMappings() {
        var tree = load();

        for (var def : tree.getClasses()) {
            intermediaryToClassMap.put(def.getName("intermediary"), def.getName("named"));

            for (var field : def.getFields()) {
                intermediaryToFieldMap.put(field.getName("intermediary"), field.getName("named"));
            }
        }
    }

    private MappingTree load() {
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

            Path intermediaryPath = mappingsDir.resolve("intermediary-" + SharedConstants.getGameVersion().getName() + ".jar");

            FileUtils.copyURLToFile(new URL(INTERMEDIARY_ENDPOINT), intermediaryPath.toFile());

            try (FileSystem fs = FileSystems.newFileSystem(intermediaryPath, (ClassLoader) null);
                 BufferedReader br = Files.newBufferedReader(fs.getPath("mappings/mappings.tiny"))) {
                Tiny2Reader.read(br, tree);
            }

            JsonArray versions = JsonHelper.getArray(DownloadUtil.read(VERSION_MANIFEST_ENDPOINT), "versions");
            String chosenUrl = null;

            for (int i = 0; i < versions.size(); i++) {
                JsonObject version = versions.get(i).getAsJsonObject();

                if (JsonHelper.getString(version, "id").equals(SharedConstants.getGameVersion().getId()))
                    chosenUrl = JsonHelper.getString(version, "url");
            }

            if (chosenUrl == null)
                throw new UnsupportedOperationException("Couldn't find version " + SharedConstants.getGameVersion().getId() + " on Mojang's servers!");

            JsonObject manifest = DownloadUtil.read(chosenUrl);
            JsonObject downloads = JsonHelper.getObject(manifest, "downloads");
            JsonObject clientMappings = JsonHelper.getObject(downloads, "client_mappings");
            JsonObject serverMappings = JsonHelper.getObject(downloads, "server_mappings");
            var sw = new MappingSourceNsSwitch(tree, "official");

            readProGuardInto(JsonHelper.getString(clientMappings, "url"), sw);
            readProGuardInto(JsonHelper.getString(serverMappings, "url"), sw);

            try (BufferedWriter bw = Files.newBufferedWriter(mojPath)) {
                tree.accept(new Tiny2Writer(bw, false));
            }

            return tree;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readProGuardInto(String url, MappingVisitor visitor) throws IOException {
        URLConnection connection = new URL(url).openConnection();
        try (var is = connection.getInputStream()) {
            ProGuardReader.read(new InputStreamReader(is), "named", "official", visitor);
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
}
