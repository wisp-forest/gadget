package io.wispforest.gadget.decompile.remap;

import net.fabricmc.mappingio.tree.MappingTree;
import org.objectweb.asm.commons.Remapper;

public class GadgetRemapper extends Remapper {
    private final RemapperStore store;
    private final MappingTree tree;
    private final int srcId;
    private final int dstId;

    public GadgetRemapper(RemapperStore store, MappingTree tree, String src, String dst) {
        this.store = store;
        this.tree = tree;
        this.srcId = tree.getNamespaceId(src);
        this.dstId = tree.getNamespaceId(dst);
    }

    @Override
    public String map(String internalName) {
        var c = tree.getClass(internalName, srcId);

        if (c == null) return internalName;

        return c.getName(dstId);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        var c = store.getClass(owner);

        if (c == null) return name;

        var f = c.member(MemberType.FIELD, name, descriptor);

        if (f == null) return name;

        var treeC = tree.getClass(f.owner(), srcId);

        if (treeC == null) return name;

        var treeF = treeC.getField(name, descriptor, srcId);

        if (treeF == null) return name;

        String targetName = treeF.getName(dstId);

        return targetName == null ? name : targetName;

    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        var c = store.getClass(owner);

        if (c == null) return name;

        var m = c.member(MemberType.FIELD, name, descriptor);

        if (m == null) return name;

        var treeC = tree.getClass(m.owner(), srcId);

        if (treeC == null) return name;

        var treeM = treeC.getMethod(name, descriptor, srcId);

        if (treeM == null) return name;

        String targetName = treeM.getName(dstId);

        return targetName == null ? name : targetName;
    }
}
