package io.wispforest.gadget.decompile.handle;


import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.decompile.KnotUtil;
import io.wispforest.gadget.decompile.OpenedURLClassLoader;
import org.jetbrains.java.decompiler.main.Fernflower;

import java.util.HashMap;
import java.util.Map;

public class QuiltflowerHandlerImpl implements io.wispforest.gadget.decompile.QuiltflowerHandler {
    public static QuiltflowerHandlerImpl INSTANCE = new QuiltflowerHandlerImpl();

    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();

    public QuiltflowerHandlerImpl() {
    }

    public byte[] getClassBytes(String name) {
        return classBytecodeStash.computeIfAbsent(name.replace('/', '.'),
            name2 -> KnotUtil.getPostMixinClassByteArray(name2, true));
    }

    @Override
    public String decompileClass(Class<?> klass) {
        GadgetResultSaver resultSaver = new GadgetResultSaver();
        Fernflower fernflower = new Fernflower(resultSaver, Map.of("ind", "    "), new GadgetFernflowerLogger());

        fernflower.addSource(new ClassContextSource(this, klass));
//        fernflower.addLibrary();

        fernflower.decompileContext();
        return resultSaver.saved;
    }

    static {
        var cl = (OpenedURLClassLoader) QuiltflowerHandlerImpl.class.getClassLoader();

        if (cl == Gadget.class.getClassLoader())
            throw new UnsupportedOperationException("Quiltflower handler was loaded on Knot!");
    }
}
