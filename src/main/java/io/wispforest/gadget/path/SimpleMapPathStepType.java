package io.wispforest.gadget.path;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.wispforest.gadget.util.PrettyPrinters;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.network.serialization.PacketBufSerializer;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record SimpleMapPathStepType(Function<String, Object> fromImpl, Function<Object, String> toImpl) implements MapPathStepType {
    private static final BiMap<String, SimpleMapPathStepType> REGISTRY = HashBiMap.create();
    private static final Map<Class<?>, SimpleMapPathStepType> CLASS_TO_TYPE = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> void register(String name, Class<T> klass, Function<String, T> fromImpl, Function<T, String> toImpl) {
        var type = new SimpleMapPathStepType((Function<String, Object>) fromImpl, (Function<Object, String>) toImpl);

        REGISTRY.put(name, type);
        CLASS_TO_TYPE.put(klass, type);
    }

    private static <T> void registerForRegistry(Class<T> klass, Registry<T> registry) {
        register(registry.getKey().getValue().toString(), klass, x -> registry.get(new Identifier(x)), x -> registry.getId(x).toString());
    }

    public static SimpleMapPathStepType getFor(Class<?> klass) {
        return ReflectionUtil.findFor(klass, CLASS_TO_TYPE);
    }

    public static void init() {}

    static {
        register("int", Integer.class, Integer::parseInt, Object::toString);
        register("string", String.class, x -> x, String::toString);
        register("identifier", Identifier.class, Identifier::new, Identifier::toString);

        registerForRegistry(Block.class, Registries.BLOCK);
        registerForRegistry(Item.class, Registries.ITEM);
        registerForRegistry(StatusEffect.class, Registries.STATUS_EFFECT);

        PacketBufSerializer.register(SimpleMapPathStepType.class, new PacketBufSerializer<>(
            (buf, type) -> buf.writeString(REGISTRY.inverse().get(type)),
            buf -> REGISTRY.get(buf.readString())));
    }

    @Override
    public Object fromNetwork(String data) {
        return fromImpl.apply(data);
    }

    @Override
    public String toNetwork(Object obj) {
        return toImpl.apply(obj);
    }

    @Override
    public String toPretty(Object obj) {
        return PrettyPrinters.tryPrint(obj);
    }
}
