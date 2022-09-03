package me.basiqueevangelist.gadget.path;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record MapPathStepType(Function<String, Object> fromImpl, Function<Object, String> toImpl) {
    private static final BiMap<String, MapPathStepType> REGISTRY = HashBiMap.create();
    private static final Map<Class<?>, MapPathStepType> CLASS_TO_TYPE = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void register(String name, Class<T> klass, Function<String, T> fromImpl, Function<T, String> toImpl) {
        var type = new MapPathStepType((Function<String, Object>) fromImpl, (Function<Object, String>) toImpl);

        REGISTRY.put(name, type);
        CLASS_TO_TYPE.put(klass, type);
    }

    private static <T> void registerForRegistry(Class<T> klass, Registry<T> registry) {
        register(registry.getKey().getValue().toString(), klass, x -> registry.get(new Identifier(x)), x -> registry.getId(x).toString());
    }

    public static MapPathStepType getFor(Class<?> klass) {
        return ReflectionUtil.findFor(klass, CLASS_TO_TYPE);
    }

    public static void init() {}

    static {
        register("int", Integer.class, Integer::parseInt, Object::toString);
        register("string", String.class, x -> x, String::toString);
        register("identifier", Identifier.class, Identifier::new, Identifier::toString);

        registerForRegistry(Block.class, Registry.BLOCK);
        registerForRegistry(Item.class, Registry.ITEM);
        registerForRegistry(StatusEffect.class, Registry.STATUS_EFFECT);

        PacketBufSerializer.register(MapPathStepType.class, new PacketBufSerializer<>(
            (buf, mapPathStepType) -> buf.writeString(REGISTRY.inverse().get(mapPathStepType)),
            buf -> REGISTRY.get(buf.readString())));
    }
}
