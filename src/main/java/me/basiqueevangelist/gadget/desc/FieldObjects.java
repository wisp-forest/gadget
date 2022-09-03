package me.basiqueevangelist.gadget.desc;

import me.basiqueevangelist.gadget.network.FieldData;
import me.basiqueevangelist.gadget.util.FieldPath;
import me.basiqueevangelist.gadget.util.HiddenFields;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class FieldObjects {
    private static final Map<Class<?>, Function<Object, String>> PRINTERS = new HashMap<>();
    private FieldObjects() {

    }

    public static Map<FieldPath, FieldData> collectAllData(FieldPath basePath, Object o) {
        Map<FieldPath, FieldData> fields = new LinkedHashMap<>();

        if (o instanceof Iterable<?> iter) {
            int i = 0;

            for (Object sub : iter) {
                int idx = i++;
                var path = basePath.then(String.valueOf(idx));

                FieldObject obj = FieldObjects.fromObject(sub);

                fields.put(path, new FieldData(obj, false));
            }
        }

        if (o.getClass().isArray()) {
            int size = Array.getLength(o);

            for (int i = 0; i < size; i++) {
                var path = basePath.then(String.valueOf(i));

                FieldObject obj = FieldObjects.fromObject(Array.get(o, i));

                fields.put(path, new FieldData(obj, false));
            }

            return fields;
        }

        for (Field field : ReflectionUtil.allFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (HiddenFields.isHidden(field)) continue;
            if (field.isSynthetic()) continue;

            var path = basePath.then(field.getName());

            FieldObject obj;

            try {
                if (!field.canAccess(o)) {
                    field.setAccessible(true);
                }

                Object value = field.get(o);

                obj = FieldObjects.fromObject(value);
            } catch (InaccessibleObjectException unused) {
                continue;
            } catch (Exception e) {
                obj = ErrorFieldObject.fromException(e);
            }

            boolean isMixin = field.getAnnotation(MixinMerged.class) != null;

            fields.put(path, new FieldData(obj, isMixin));
        }

        return fields;
    }


    public static FieldObject fromObject(Object o) {
        if (o == null)
            return new SimpleFieldObject("null");

        if (o instanceof String str)
            return new SimpleFieldObject("\"" + str + "\"");

        var printer = ReflectionUtil.findFor(o.getClass(), PRINTERS);

        if (printer != null)
            return new SimpleFieldObject(printer.apply(o));

        if (o.getClass().isEnum())
            return new ComplexFieldObject(ReflectionUtil.prettyName(o.getClass()) + "#" + ((Enum<?>) o).name());

        return new ComplexFieldObject(ReflectionUtil.prettyName(o.getClass()));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    private static <T> void register(Function<T, String> printer, Class<? extends T>... classes) {
        for (Class<?> klass : classes) {
            PRINTERS.put(klass, (Function<Object, String>) printer);
        }
    }

    static {
        register(Object::toString,
            // Standard library classes
            Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            Character.class, Class.class,

            AtomicBoolean.class, AtomicInteger.class, AtomicLong.class,

            // Minecraft classes
            BlockState.class, FluidState.class, World.class);

        register(x -> "\"" + x + "\"", String.class);

        register(x -> Registry.ITEM.getId(x).toString(), Item.class);
        register(x -> Registry.BLOCK.getId(x).toString(), Block.class);
    }
}
