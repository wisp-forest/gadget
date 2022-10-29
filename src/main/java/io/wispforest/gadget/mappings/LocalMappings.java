package io.wispforest.gadget.mappings;

import com.google.common.base.Suppliers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class LocalMappings implements Mappings {
    public static final LocalMappings INSTANCE = new LocalMappings();

    private LocalMappings() {

    }

    private final Supplier<Map<String, String>> intermediaryToClassMap = Suppliers.memoize(() -> {
        var mappings = MappingsManager.runtimeMappings();
        Map<String, String> inverse = new HashMap<>();

        for (var def : mappings.getClasses()) {
            inverse.put(def.getName("intermediary"), def.getName(MappingsManager.runtimeNamespace()));
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

    @Override
    public String mapClass(String src) {
        src = src.replace('.', '/');

        return intermediaryToClassMap.get().getOrDefault(src, src).replace('/', '.');
    }

    @Override
    public String mapField(String src) {
        return intermediaryToFieldMap.get().getOrDefault(src, src);
    }
}
