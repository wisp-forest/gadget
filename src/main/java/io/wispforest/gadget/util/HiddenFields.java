package io.wispforest.gadget.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.util.List;

public class HiddenFields {
    private static final List<Field> HIDDEN;

    public static boolean isHidden(Field field) {
        return HIDDEN.contains(field);
    }

    private static String getFieldName(String owner, String name, String desc) {
        return FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", owner, name, desc);
    }

    static {
        try {
            HIDDEN = List.of(
                Enum.class.getDeclaredField("name"),
                Enum.class.getDeclaredField("ordinal"),

                BlockEntity.class.getDeclaredField(getFieldName("net.minecraft.class_2586", "field_11866", "Lnet/minecraft/class_2680;")),
                BlockEntity.class.getDeclaredField(getFieldName("net.minecraft.class_2586", "field_11865", "Z")),
                BlockEntity.class.getDeclaredField(getFieldName("net.minecraft.class_2586", "field_11864", "Lnet/minecraft/class_2591;")),

                Entity.class.getDeclaredField(getFieldName("net.minecraft.class_1297", "field_5961", "Lnet/minecraft/class_1299;"))
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
