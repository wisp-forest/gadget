package io.wispforest.gadget.mappings;

import com.google.common.base.Suppliers;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.GadgetConfigModel;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class MappingsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("gadget/MappingsManager");
    private static final Supplier<Map<String, ClassDef>> intermediaryClassMap = Suppliers.memoize(() -> {
        var mappings = runtimeMappings();
        Map<String, ClassDef> inverse = new HashMap<>();

        for (var def : mappings.getClasses()) {
            inverse.put(def.getName("intermediary"), def);
        }

        return inverse;
    });

    private static Mappings DISPLAY_MAPPINGS;

    private MappingsManager() {

    }

    public static void init() {
        reloadMappings();

        Gadget.CONFIG.subscribeToMappings(type -> reloadMappings());
    }

    private static void reloadMappings() {
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

    public static TinyTree runtimeMappings() {
        return FabricLauncherBase.getLauncher().getMappingConfiguration().getMappings();
    }

    public static Mappings displayMappings() {
        return DISPLAY_MAPPINGS;
    }

    public static String remapClassToDisplay(String name) {
        return displayMappings().mapClass(unmapClass(name));
    }

    public static String unmapClass(String name) {
        return FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", name);
    }

    public static String unmapField(Field field) {
        if (!FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named")) return field.getName();

        var ownerUnmapped = FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", field.getDeclaringClass().getName());
        ClassDef owner = intermediaryClassMap.get().get(ownerUnmapped.replace('.', '/'));

        if (owner == null)
            return field.getName();

        for (var other : owner.getFields()) {
            if (other.getName(runtimeNamespace()).equals(field.getName()))
                return other.getName("intermediary");
        }

        return field.getName();
    }
}
