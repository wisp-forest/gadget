package io.wispforest.gadget.mappings;

import net.fabricmc.mappingio.tree.MappingTreeView;

import java.util.HashMap;
import java.util.Map;

public final class MappingUtils {
    private MappingUtils() {

    }

    public static Map<String, String> createFieldIdUnmap(MappingTreeView tree, String to) {
        Map<String, String> map = new HashMap<>();

        for (var klass : tree.getClasses()) {
            String fromName = klass.getName("intermediary");
            if (fromName == null) fromName = klass.getSrcName();
            fromName = fromName.replace('/', '.');

            for (var field : klass.getFields()) {
                String intermediary =
                    fromName
                        + "#"
                        + field.getName("intermediary");
                String local =
                    klass.getName(to).replace('/', '.')
                        + "#"
                        + field.getName(to);

                map.put(local, intermediary);
            }
        }

        return map;
    }
}
