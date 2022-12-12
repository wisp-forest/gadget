package io.wispforest.gadget.decompile.handle;


import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.decompile.OpenedURLClassLoader;
import io.wispforest.gadget.decompile.fs.ClassesFileSystem;
import io.wispforest.gadget.mappings.LocalMappings;
import io.wispforest.gadget.mappings.MappingUtils;
import io.wispforest.gadget.mappings.MappingsManager;
import net.auoeke.reflect.Constructors;
import net.auoeke.reflect.Invoker;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.TinyRemapper;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QuiltflowerHandlerImpl implements io.wispforest.gadget.decompile.QuiltflowerHandler {
    private final TinyRemapper tinyRemapper = TinyRemapper.newRemapper()
        .withMappings(acceptor -> {
            try {
                MemoryMappingTree tree = new MemoryMappingTree();

                MappingsManager.runtimeMappings().accept(tree);
                MappingsManager.displayMappings()
                    .load(new MappingNsRenamer(tree, Map.of(MappingsManager.runtimeNamespace(), "target")));

                MappingUtils.feedMappings(acceptor, tree, MappingsManager.runtimeNamespace(), "target");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        })
        .threads(1)
        .build();
    final ClassesFileSystem fs = new ClassesFileSystem(tinyRemapper.getEnvironment().getRemapper());
    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();

    public QuiltflowerHandlerImpl() {
        tinyRemapper.readClassPath(fs.getRootDirectories().iterator().next());
    }

    @Override
    public String mapClass(String name) {
        return tinyRemapper.getEnvironment().getRemapper().map(name);
    }

    @Override
    public String unmapClass(String name) {
        return LocalMappings.INSTANCE.mapClass(MappingsManager.displayMappings().unmapClass(name));
    }

    public byte[] getClassBytes(String name) {
        return classBytecodeStash.computeIfAbsent(name, unused -> {
            String remapped = unmapClass(name);
            byte[] bytes = fs.getBytes(remapped);
            ClassReader reader = new ClassReader(bytes);
            ClassWriter cw = new ClassWriter(0);

            try {
                var mh = Constructors.of(Class.forName("net.fabricmc.tinyremapper.AsmClassRemapper"))
                    .map(Invoker::unreflectConstructor)
                    .findFirst()
                    .orElseThrow();

                reader.accept(
                    (ClassVisitor) mh.invokeWithArguments(
                        cw, tinyRemapper.getEnvironment().getRemapper(),
                        false, false, false, false, null, false), 0);
            } catch (Throwable cnfe) {
                throw new RuntimeException(cnfe);
            }
            Gadget.LOGGER.info("Remapped {}", name);
            return cw.toByteArray();
        });
    }

    @Override
    public String decompileClass(Class<?> klass) {
        GadgetResultSaver resultSaver = new GadgetResultSaver();
        Fernflower fernflower = new Fernflower(resultSaver, Map.of("ind", "    "), new GadgetFernflowerLogger());

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
