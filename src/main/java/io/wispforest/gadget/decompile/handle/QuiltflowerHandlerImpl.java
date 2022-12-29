package io.wispforest.gadget.decompile.handle;


import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.decompile.OpenedURLClassLoader;
import io.wispforest.gadget.decompile.fs.ClassesFileSystem;
import io.wispforest.gadget.decompile.remap.RemapperStore;
import io.wispforest.gadget.mappings.LocalMappings;
import io.wispforest.gadget.mappings.MappingsManager;
import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.minecraft.text.Text;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QuiltflowerHandlerImpl implements io.wispforest.gadget.decompile.QuiltflowerHandler {
    private final MemoryMappingTree mappings;
    private final RemapperStore remapperStore;
    final ClassesFileSystem fs;
    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();
    final Consumer<Text> logConsumer;

    public QuiltflowerHandlerImpl(ProgressToast toast, Consumer<Text> logConsumer) {
        this.logConsumer = logConsumer;
        this.fs = new ClassesFileSystem();
        this.remapperStore = new RemapperStore(this.fs::getBytes);

        toast.step(Text.translatable("message.gadget.progress.loading_mappings"));
        mappings = new MemoryMappingTree(true);

        try {
            MappingsManager.runtimeMappings().accept(mappings);
            MappingsManager.displayMappings()
                .load(new MappingNsRenamer(mappings, Map.of("named", "target")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        mappings.visitNamespaces("intermediary", List.of("source"));

        for (MappingTree.ClassMapping c : mappings.getClasses()) {
            String cName = c.getName("named");
            if (cName == null) cName = c.getName("intermediary");

            c.setDstName(cName, mappings.getNamespaceId("source"));

            for (MappingTree.FieldMapping f : c.getFields()) {
                String fName = f.getName("named");
                if (fName == null) fName = f.getName("intermediary");

                f.setDstName(fName, mappings.getNamespaceId("source"));
            }

            for (MappingTree.MethodMapping m : c.getMethods()) {
                String mName = m.getName("named");
                if (mName == null) mName = m.getName("intermediary");

                m.setDstName(mName, mappings.getNamespaceId("source"));
            }
        }
    }

    @Override
    public String mapClass(String name) {
        MappingTree.ClassMapping c = mappings.getClass(name, mappings.getNamespaceId("source"));

        if (c == null) return name;

        String mapped = c.getName("target");

        return mapped == null ? name : mapped;
    }

    @Override
    public String unmapClass(String name) {
        return LocalMappings.INSTANCE.mapClass(MappingsManager.displayMappings().unmapClass(name));
    }

    @Override
    public byte[] getClassBytes(String name) {
        return classBytecodeStash.computeIfAbsent(name, unused -> {
            String remapped = unmapClass(name);
            byte[] bytes = fs.getBytes(remapped);

            if (name.startsWith("java/"))
                return bytes;

            ClassReader reader = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(0);

            try {
                var remapper = remapperStore.createRemapper(mappings, "source", "target");
                reader.accept(new ClassRemapper(cw, remapper), 0);
            } catch (Throwable cnfe) {
                throw new RuntimeException(cnfe);
            }
            return cw.toByteArray();
        });
    }

    @Override
    public String decompileClass(Class<?> klass) {
        GadgetResultSaver resultSaver = new GadgetResultSaver();
        Fernflower fernflower = new Fernflower(resultSaver, Map.of("ind", "    "), new GadgetFernflowerLogger(this));

        fernflower.addSource(new ClassContextSource(this, klass));
        fernflower.addLibrary(new EverythingContextSource(this));
//        fernflower.addLibrary();

        fernflower.decompileContext();
        fernflower.clearContext();
        return resultSaver.saved;
    }

    static {
        var cl = (OpenedURLClassLoader) QuiltflowerHandlerImpl.class.getClassLoader();

        if (cl == Gadget.class.getClassLoader())
            throw new UnsupportedOperationException("Quiltflower handler was loaded on Knot!");
    }
}
