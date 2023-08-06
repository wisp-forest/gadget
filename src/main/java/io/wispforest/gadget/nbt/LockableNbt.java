package io.wispforest.gadget.nbt;

public interface LockableNbt {
    void lock(NbtLock lock);

    void unlock(NbtLock lock);
}
