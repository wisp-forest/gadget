package io.wispforest.gadget.desc;

import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.mappings.MappingsManager;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.*;
import io.wispforest.gadget.util.HiddenFields;
import io.wispforest.gadget.util.PrettyPrinters;
import io.wispforest.gadget.util.ReflectionUtil;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class FieldObjects {
    private FieldObjects() {

    }

    public static Map<ObjectPath, FieldData> collectAllData(ObjectPath basePath, Object o) {
        Map<ObjectPath, FieldData> fields = new LinkedHashMap<>();

        if (o instanceof Iterable<?> iter) {
            int i = 0;
            boolean isFinal = ReflectionUtil.guessImmutability(iter);

            for (Object sub : iter) {
                int idx = i++;
                var path = basePath.then(new IndexPathStep(idx));

                FieldObject obj = FieldObjects.fromObject(sub);

                fields.put(path, new FieldData(obj, false, isFinal));
            }
        }

        if (o instanceof Map<?, ?> map
         && !map.isEmpty()) {
            var oneKey = map.keySet().iterator().next();
            boolean isFinal = ReflectionUtil.guessImmutability(map);

            MapPathStepType type = MapPathStepType.getFor(oneKey.getClass());

            if (type != null) {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    var path = basePath.then(new MapPathStep(type, type.toNetwork(entry.getKey())));

                    FieldObject obj = FieldObjects.fromObject(entry.getValue());

                    fields.put(path, new FieldData(obj, false, isFinal));
                }
            }
        }

        if (o.getClass().isArray()) {
            int size = Array.getLength(o);

            for (int i = 0; i < size; i++) {
                var path = basePath.then(new IndexPathStep(i));

                FieldObject obj = FieldObjects.fromObject(Array.get(o, i));

                fields.put(path, new FieldData(obj, false, false));
            }

            return fields;
        }

        for (Field field : ReflectionUtil.allFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (HiddenFields.isHidden(field)) continue;
            if (field.isSynthetic()) continue;

            var path = basePath.then(FieldPathStep.forField(field));

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

            fields.put(path, new FieldData(obj, isMixin, false));
        }

        return fields;
    }


    public static FieldObject fromObject(Object o) {
        if (o instanceof NbtCompound compound)
            return new NbtCompoundFieldObject(compound);

        String pretty = PrettyPrinters.tryPrint(o);

        if (pretty != null)
            return new PrimitiveFieldObject(pretty, Optional.ofNullable(PrimitiveEditData.forObject(o)));

        String unmappedClass = MappingsManager.unmapClass(ReflectionUtil.prettyName(o.getClass()));

        if (o.getClass().isEnum())
            return new ComplexFieldObject(unmappedClass, "#" + ((Enum<?>) o).name());

        return new ComplexFieldObject(unmappedClass, "");
    }
}
