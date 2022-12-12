package io.wispforest.gadget.mappings;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.mappingio.MappingVisitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class LocalMappings implements Mappings {
    public static final LocalMappings INSTANCE = new LocalMappings();

    private LocalMappings() {

    }

    private final Supplier<BiMap<String, String>> intermediaryToClassMap = Suppliers.memoize(() -> {
        var mappings = MappingsManager.runtimeMappings();
        BiMap<String, String> inverse = HashBiMap.create();

        for (var def : mappings.getClasses()) {
            inverse.put(
                def.getName("intermediary").replace('/', '.'),
                def.getName(MappingsManager.runtimeNamespace()).replace('/', '.')
            );
        }

        return inverse;
    });

    private final Supplier<BiMap<String, String>> intermediaryFromClassMap = Suppliers.memoize(() -> {
        var mappings = MappingsManager.runtimeMappings();
        BiMap<String, String> inverse = HashBiMap.create();

        for (var def : mappings.getClasses()) {
            inverse.put(
                def.getName(MappingsManager.runtimeNamespace()).replace('/', '.'),
                def.getName("intermediary").replace('/', '.')
            );
        }

        return inverse;
    });

    private final Supplier<Map<String, String>> intermediaryToFieldMap = Suppliers.memoize(() -> {
        var mappings = MappingsManager.runtimeMappings();
        Map<String, String> map = new HashMap<>();

        for (var klass : mappings.getClasses()) {
            for (var field : klass.getFields()) {
                map.put(field.getName("intermediary"), field.getName(MappingsManager.runtimeNamespace()));
            }
        }

        return map;
    });

    private final Supplier<Map<String, String>> fieldIdToIntermediary = Suppliers.memoize(
        () -> MappingUtils.createFieldIdUnmap(MappingsManager.runtimeMappings(), MappingsManager.runtimeNamespace()));

    @Override
    public String mapClass(String src) {
        return intermediaryToClassMap.get().getOrDefault(src, src);
    }

    @Override
    public String mapField(String src) {
        return intermediaryToFieldMap.get().getOrDefault(src, src);
    }

    @Override
    public String unmapClass(String dst) {
        return intermediaryFromClassMap.get().getOrDefault(dst, dst);
    }

    @Override
    public String unmapFieldId(String dst) {
        return fieldIdToIntermediary.get().getOrDefault(dst, dst);
    }

    @Override
    public void load(MappingVisitor visitor) throws IOException {
        MappingsManager.runtimeMappings().accept(visitor);
    }
}
