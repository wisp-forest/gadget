package io.wispforest.gadget.desc.edit;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RegistryEditType<T> implements PrimitiveEditType<T> {
    private final Registry<T> registry;

    public RegistryEditType(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public T fromPacket(String repr) {
        Identifier id = Identifier.tryParse(repr);

        if (id == null) return null;

        return registry.get(id);
    }

    @Override
    public String toPacket(T value) {
        return registry.getId(value).toString();
    }
}
