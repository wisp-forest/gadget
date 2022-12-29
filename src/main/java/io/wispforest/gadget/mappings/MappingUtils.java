package io.wispforest.gadget.mappings;

import net.fabricmc.mappingio.tree.MappingTree;
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

    public static String fieldTargetName(MappingTree tree, int srcId, int dstId, String owner, String name,
                                         String desc, boolean defaultToOriginal) {
        var treeC = tree.getClass(owner, srcId);

        if (treeC == null) return defaultToOriginal ? name : null;

        var treeF = treeC.getField(name, desc, srcId);

        if (treeF == null) return defaultToOriginal ? name : null;

        String targetName = treeF.getName(dstId);

        return (targetName != null || !defaultToOriginal) ? targetName : name;
    }

    public static String methodTargetName(MappingTree tree, int srcId, int dstId, String owner, String name,
                                         String desc, boolean defaultToOriginal) {
        var treeC = tree.getClass(owner, srcId);

        if (treeC == null) return defaultToOriginal ? name : null;

        var treeM = treeC.getMethod(name, desc, srcId);

        if (treeM == null) return defaultToOriginal ? name : null;

        String targetName = treeM.getName(dstId);

        return (targetName != null || !defaultToOriginal) ? targetName : name;
    }
}
