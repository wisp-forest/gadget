package me.basiqueevangelist.gadget.desc;

import me.basiqueevangelist.gadget.network.FieldData;
import me.basiqueevangelist.gadget.util.FieldPath;
import me.basiqueevangelist.gadget.util.HiddenFields;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class FieldObjects {
    private static final List<Class<?>> SIMPLE_CLASSES = List.of(
        // Standard library classes
        String.class, Boolean.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
        Character.class, Class.class,

        AtomicBoolean.class, AtomicInteger.class, AtomicLong.class,

        // Minecraft classes
        BlockState.class, FluidState.class, ServerWorld.class, ClientWorld.class
    );

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

        if (SIMPLE_CLASSES.contains(o.getClass()))
            return new SimpleFieldObject(o.toString());

        if (o.getClass().isEnum()) {
            return new ComplexFieldObject(o.getClass().getName() + "#" + ((Enum<?>) o).name());
        }

        return new ComplexFieldObject(o.getClass().getName());
    }
}
