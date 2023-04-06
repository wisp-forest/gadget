package io.wispforest.gadget.field;

import io.wispforest.gadget.desc.FieldObjects;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public record LocalFieldDataSource(Object target, boolean isMutable) implements FieldDataSource {
    @Override
    public FieldData rootData() {
        return new FieldData(FieldObjects.fromObject(target), false, true);
    }

    @Override
    public Map<PathStep, FieldData> initialRootFields() {
        return FieldObjects.getData(target, 0, -1);
    }

    @Override
    public CompletableFuture<Map<PathStep, FieldData>> requestFieldsOf(ObjectPath path, int from, int limit) {
        Object sub = path.follow(target);

        return CompletableFuture.completedFuture(FieldObjects.getData(sub, from, limit));
    }

    @Override
    public boolean isMutable() {
        return isMutable;
    }

    @Override
    public CompletableFuture<Void> setPrimitiveAt(ObjectPath path, PrimitiveEditData editData) {
        if (!isMutable())
            throw new UnsupportedOperationException();

        path.set(target, editData.toObject());

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setNbtCompoundAt(ObjectPath path, NbtCompound tag) {
        if (!isMutable())
            throw new UnsupportedOperationException();

        path.set(target, tag);

        return CompletableFuture.completedFuture(null);
    }
}
