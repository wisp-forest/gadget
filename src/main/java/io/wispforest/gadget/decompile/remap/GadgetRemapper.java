package io.wispforest.gadget.decompile.remap;

import io.wispforest.gadget.mappings.MappingUtils;
import net.fabricmc.mappingio.tree.MappingTree;
import org.objectweb.asm.commons.Remapper;

public class GadgetRemapper extends Remapper {
    private final RemapperStore store;
    private final MappingTree tree;
    private final int srcId;
    private final int dstId;

    public GadgetRemapper(RemapperStore store, MappingTree tree, int srcId, int dstId) {
        this.store = store;
        this.tree = tree;
        this.srcId = srcId;
        this.dstId = dstId;
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

        return MappingUtils.fieldTargetName(tree, srcId, dstId, owner, name, descriptor, true);

    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        var c = store.getClass(owner);

        if (c == null) return name;

        var m = c.member(MemberType.METHOD, name, descriptor);

        if (m == null) return name;

        return MappingUtils.methodTargetName(tree, srcId, dstId, owner, name, descriptor, true);
    }
}
