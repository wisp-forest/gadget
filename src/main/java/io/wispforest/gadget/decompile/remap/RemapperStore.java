package io.wispforest.gadget.decompile.remap;

import net.fabricmc.mappingio.tree.MappingTree;
import org.objectweb.asm.ClassReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RemapperStore {
    private final Map<String, AnalyzedClass> analyzedClasses = new HashMap<>();
    private final Function<String, byte[]> bytecodeProvider;

    public RemapperStore(Function<String, byte[]> bytecodeProvider) {
        this.bytecodeProvider = bytecodeProvider;
    }

    public GadgetRemapper createRemapper(MappingTree tree, String src, String dst) {
        return new GadgetRemapper(this, tree, src, dst);
    }

    public AnalyzedClass getClass(String internalName) {
        var klass = analyzedClasses.get(internalName);

        if (klass == null) {
            klass = analyze(internalName);
            analyzedClasses.put(internalName, klass);
        }

        return klass;
    }

    private AnalyzedClass analyze(String name) {
        if (name.startsWith("[")) {
            return new AnalyzedClass(name, getClass("java/lang/Object"),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        }

        byte[] bytes = bytecodeProvider.apply(name);
        if (bytes == null) return null;

        ClassReader reader = new ClassReader(bytes);
        ClassAnalyzer analyzer = new ClassAnalyzer(this);
        reader.accept(analyzer, 0);
        return analyzer.build();
    }
}
