package io.wispforest.gadget.decompile;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public final class KnotUtil {
    private static final Object KNOT_DELEGATE;
    private static final MethodHandle POST_MIXIN_BYTES_GETTER;

    private KnotUtil() {

    }

    public static byte[] getPostMixinClassByteArray(String name, boolean allowFromParent) {
        try {
            return (byte[]) POST_MIXIN_BYTES_GETTER.invokeExact(name, allowFromParent);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            var launcher = FabricLauncherBase.getLauncher();
            Field delegateField = launcher.getClass().getDeclaredField("classLoader");
            delegateField.setAccessible(true);
            KNOT_DELEGATE = delegateField.get(launcher);

            var getter = KNOT_DELEGATE.getClass().getDeclaredMethod("getPostMixinClassByteArray", String.class, boolean.class);
            getter.setAccessible(true);
            POST_MIXIN_BYTES_GETTER = MethodHandles.lookup().unreflect(getter)
                .bindTo(KNOT_DELEGATE);
        } catch (ReflectiveOperationException roe) {
            throw new RuntimeException(roe);
        }
    }
}
