package me.basiqueevangelist.gadget.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.util.List;

public class HiddenFields {
    private static final List<Field> HIDDEN;

    public static boolean isHidden(Field field) {
        return HIDDEN.contains(field);
    }

    static {
        try {
            HIDDEN = List.of(
                Enum.class.getDeclaredField("name"),
                Enum.class.getDeclaredField("ordinal"),

                BlockEntity.class.getDeclaredField("cachedState"),
                BlockEntity.class.getDeclaredField("removed"),
                BlockEntity.class.getDeclaredField("type"),

                Entity.class.getDeclaredField("type")
            );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
