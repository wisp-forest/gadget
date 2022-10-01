package io.wispforest.gadget.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class PrettyPrinters {
    private static final Map<Class<?>, Function<Object, String>> PRINTERS = new HashMap<>();

    private PrettyPrinters() {

    }

    public static String tryPrint(Object o) {
        if (o == null)
            return "null";

        var printer = ReflectionUtil.findFor(o.getClass(), PRINTERS);

        if (printer != null)
            return printer.apply(o);
        else
            return null;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> void register(Function<T, String> printer, Class<? extends T>... classes) {
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
            BlockState.class, FluidState.class, World.class, Identifier.class);

        register(x -> "\"" + x + "\"", String.class);

        register(x -> Registry.ITEM.getId(x).toString(), Item.class);
        register(x -> Registry.BLOCK.getId(x).toString(), Block.class);
        register(x -> Registry.STATUS_EFFECT.getId(x).toString(), StatusEffect.class);
    }
}
