package io.wispforest.gadget.decompile.remap;

import net.fabricmc.mappingio.tree.MappingTree;
import net.minecraft.text.Text;
import org.objectweb.asm.ClassReader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RemapperStore {
    private final Map<String, AnalyzedClass> analyzedClasses = new HashMap<>();
    private final Function<String, byte[]> bytecodeProvider;
    private final Consumer<Text> logConsumer;
    final MappingTree tree;
    final int srcId;
    final int dstId;

    public RemapperStore(Function<String, byte[]> bytecodeProvider, Consumer<Text> logConsumer, MappingTree tree,
                         String src, String dst) {
        this.bytecodeProvider = bytecodeProvider;
        this.logConsumer = logConsumer;
        this.tree = tree;
        this.srcId = tree.getNamespaceId(src);
        this.dstId = tree.getNamespaceId(dst);
    }

    public GadgetRemapper createRemapper() {
        return new GadgetRemapper(this, tree, srcId, dstId);
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

        logConsumer.accept(Text.translatable("text.gadget.log.loading_class", name));

        byte[] bytes = bytecodeProvider.apply(name);
        if (bytes == null) return null;

        ClassReader reader = new ClassReader(bytes);
        ClassAnalyzer analyzer = new ClassAnalyzer(this);
        reader.accept(analyzer, 0);
        return analyzer.build();
    }
}
