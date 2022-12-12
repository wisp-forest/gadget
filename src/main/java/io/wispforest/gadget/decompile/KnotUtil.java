package io.wispforest.gadget.decompile;

import net.auoeke.reflect.ClassTransformer;
import net.auoeke.reflect.Reflect;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public final class KnotUtil {
    private static final Instrumentation INSTRUMENTATION = Reflect
        .instrument()
        .valueOr(null);
    private static final Object KNOT_DELEGATE;
    private static final MethodHandle POST_MIXIN_BYTES_GETTER;

    private KnotUtil() {

    }

    public static byte[] getPostMixinClassByteArray(String name, boolean allowFromParent) {
        if (INSTRUMENTATION != null) {
            var transformer = new ClassTransformer() {
                private byte[] bytes;

                @Override
                public byte[] transform(Module module, ClassLoader loader, String name1, Class<?> type, ProtectionDomain domain, byte[] classFile) {
                    if (name.equals(name1))
                        bytes = classFile;

                    return null;
                }
            };

            INSTRUMENTATION.addTransformer(transformer.singleUse(INSTRUMENTATION));
            try {
                INSTRUMENTATION.retransformClasses(Class.forName(name));

                if (transformer.bytes != null)
                    return transformer.bytes;
            } catch (ClassNotFoundException | UnmodifiableClassException e) {
                // ...
            }
        }

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
