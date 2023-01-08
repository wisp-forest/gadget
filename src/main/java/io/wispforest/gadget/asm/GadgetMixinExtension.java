package io.wispforest.gadget.asm;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.Set;
import java.util.TreeSet;

public class GadgetMixinExtension implements IExtension {
    public static final Set<String> DUMPED_CLASSES = new TreeSet<>();

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Override
    public void preApply(ITargetClassContext context) {

    }

    @Override
    public void postApply(ITargetClassContext context) {

    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {
        if (classNode.nestHostClass != null) {
            DUMPED_CLASSES.add(classNode.nestHostClass);
        } else {
            DUMPED_CLASSES.add(name);
        }
    }
}
