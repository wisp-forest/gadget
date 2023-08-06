package io.wispforest.gadget.nbt;

import io.wispforest.gadget.Gadget;

import java.util.List;
import java.util.stream.Collectors;

public interface LockableNbtInternal extends LockableNbt {
    List<NbtLock> gadget$locks();

    default void gadget$checkWrite() {
        if (!Gadget.CONFIG.nbtLocking()) return;

        if (gadget$locks().size() > 0) {
            throw new IllegalStateException("Tried to mutate NBT tag while locked by " + gadget$locks()
                .stream()
                .map(NbtLock::name)
                .collect(Collectors.joining(", ")));
        }
    }
}
