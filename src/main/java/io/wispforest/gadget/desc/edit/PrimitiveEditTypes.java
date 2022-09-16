package io.wispforest.gadget.desc.edit;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PrimitiveEditTypes {
    private static final BiMap<String, PrimitiveEditType<?>> REGISTRY = HashBiMap.create();
    private static final Map<Class<?>, PrimitiveEditType<?>> CLASS_TO_TYPE = new HashMap<>();

    private PrimitiveEditTypes() {

    }

    public static <T> void register(String name, Class<T> klass, PrimitiveEditType<T> type) {
        REGISTRY.put(name, type);
        CLASS_TO_TYPE.put(klass, type);
    }

    public static <T> void registerSimple(String name, Class<T> klass, Function<String, T> fromImpl, Function<T, String> toImpl) {
        register(name, klass, new SimpleEditType<>(fromImpl, toImpl));
    }

    public static <T> void registerForRegistry(Class<T> klass, Registry<T> registry) {
        register(registry.getKey().getValue().toString(), klass, new RegistryEditType<>(registry));
    }

    @SuppressWarnings("unchecked")
    public static <T> PrimitiveEditType<T> getFor(Class<T> klass) {
        return (PrimitiveEditType<T>) ReflectionUtil.findFor(klass, CLASS_TO_TYPE);
    }

    public static void init() {
        registerSimple("boolean", Boolean.class, Boolean::parseBoolean, Object::toString);
        registerSimple("int", Integer.class, Integer::parseInt, Object::toString);
        registerSimple("long", Long.class, Long::parseLong, Object::toString);
        registerSimple("float", Float.class, Float::parseFloat, Object::toString);
        registerSimple("double", Double.class, Double::parseDouble, Object::toString);
        registerSimple("string", String.class, x -> x, String::toString);
        registerSimple("identifier", Identifier.class, Identifier::new, Identifier::toString);

        registerForRegistry(Block.class, Registry.BLOCK);
        registerForRegistry(Item.class, Registry.ITEM);
        registerForRegistry(StatusEffect.class, Registry.STATUS_EFFECT);

        PacketBufSerializer.register(PrimitiveEditType.class, new PacketBufSerializer<>(
            (buf, mapPathStepType) -> buf.writeString(REGISTRY.inverse().get(mapPathStepType)),
            buf -> REGISTRY.get(buf.readString())));
    }
}
