package io.wispforest.gadget.mappings;

import com.google.common.base.Suppliers;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.GadgetConfigModel;
import io.wispforest.gadget.util.ReflectionUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public final class MappingsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("gadget/MappingsManager");
    private static @Nullable MappingTreeView runtimeMappings;
    private static final Supplier<Map<String, MappingTreeView.ClassMappingView>> intermediaryClassMap = Suppliers.memoize(() -> {
        var mappings = runtimeMappings();
        Map<String, MappingTreeView.ClassMappingView> inverse = new HashMap<>();

        for (var def : mappings.getClasses()) {
            inverse.put(def.getName("intermediary"), def);
        }

        return inverse;
    });

    private static Mappings DISPLAY_MAPPINGS = LocalMappings.INSTANCE;
    private static boolean initted = false;

    private MappingsManager() {

    }

    public static void init() {
        if (initted) return;
        initted = true;

        reloadMappings();

        Gadget.CONFIG.subscribeToMappings(type -> reloadMappings());

        CompletableFuture.runAsync(MappingsManager::runtimeMappings)
            .exceptionally(e -> {
                Gadget.LOGGER.error("Encountered error while loading runtime mappings", e);
                return null;
            });
    }

    public static void reloadMappings() {
        try {
            DISPLAY_MAPPINGS = Gadget.CONFIG.mappings().factory().get();
        } catch (Exception e) {
            LOGGER.error("Encountered error while loading {} mappings", Gadget.CONFIG.mappings(), e);
            Gadget.CONFIG.mappings(GadgetConfigModel.MappingsType.LOCAL);

            DISPLAY_MAPPINGS = LocalMappings.INSTANCE;
        }
    }

    public static String runtimeNamespace() {
        return FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace();
    }

    public static MappingTreeView runtimeMappings() {
        if (runtimeMappings != null)
            return runtimeMappings;

        URL url = FabricLoader.class.getClassLoader().getResource("mappings/mappings.tiny");

        if (url == null) {
            return runtimeMappings = new MemoryMappingTree();
        }

        try {
            var conn = url.openConnection();

            try (var is = conn.getInputStream()) {
                MemoryMappingTree tree = new MemoryMappingTree();

                MappingReader.read(new InputStreamReader(is), tree);

                return runtimeMappings = tree;
            }
        } catch (IOException e) {
            Gadget.LOGGER.error("Couldn't load runtime mappings!", e);

            return runtimeMappings = new MemoryMappingTree();
        }
    }

    public static Mappings displayMappings() {
        return DISPLAY_MAPPINGS;
    }

    public static String remapClassToDisplay(Class<?> klass) {
        return displayMappings().mapClass(unmapClass(klass));
    }

    public static String unmapClass(Class<?> klass) {
        return unmapClass(ReflectionUtil.prettyName(klass));
    }

    public static String unmapClass(String name) {
        if (name.contains("/")) {
            int slashIdx = name.lastIndexOf("/");
            return unmapClass(name.substring(0, slashIdx)) + name.substring(slashIdx);
        }

        if (name.contains("$$")) {
            int dollarDollarIdx = name.lastIndexOf("$$");
            return unmapClass(name.substring(0, dollarDollarIdx)) + name.substring(dollarDollarIdx);
        }

        return FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", name);
    }

    public static String unmapField(Field field) {
        if (!FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named")) return field.getName();

        var ownerUnmapped = unmapClass(field.getDeclaringClass());
        var owner = intermediaryClassMap.get().get(ownerUnmapped.replace('.', '/'));

        if (owner == null)
            return field.getName();

        for (var other : owner.getFields()) {
            if (other.getName(runtimeNamespace()).equals(field.getName()))
                return other.getName("intermediary");
        }

        return field.getName();
    }
}
