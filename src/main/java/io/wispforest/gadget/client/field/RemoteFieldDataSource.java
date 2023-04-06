package io.wispforest.gadget.client.field;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.field.FieldDataSource;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.network.GadgetNetworking;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.network.packet.c2s.FieldDataRequestC2SPacket;
import io.wispforest.gadget.network.packet.c2s.FieldDataSetPrimitiveC2SPacket;
import io.wispforest.gadget.network.packet.c2s.FieldDataSetNbtCompoundC2SPacket;
import io.wispforest.gadget.network.packet.s2c.FieldDataResponseS2CPacket;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import net.minecraft.nbt.NbtCompound;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

public class RemoteFieldDataSource implements FieldDataSource {
    private final InspectionTarget target;
    private final FieldData rootData;
    private final Map<PathStep, FieldData> rootFields;

    private final Map<ObjectPath, CompletableFuture<Map<PathStep, FieldData>>> pendingFieldsRequests = new TreeMap<>();

    public RemoteFieldDataSource(InspectionTarget target, FieldData rootData, Map<PathStep, FieldData> rootFields) {
        this.target = target;
        this.rootData = rootData;
        this.rootFields = rootFields;
    }

    @Override
    public FieldData rootData() {
        return rootData;
    }

    @Override
    public Map<PathStep, FieldData> initialRootFields() {
        return rootFields;
    }

    @Override
    public CompletableFuture<Map<PathStep, FieldData>> requestFieldsOf(ObjectPath path, int from, int limit) {
        CompletableFuture<Map<PathStep, FieldData>> future = new CompletableFuture<>();

        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataRequestC2SPacket(this.target, path, from, limit));

        pendingFieldsRequests.put(path, future);

        return future;
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public CompletableFuture<Void> setPrimitiveAt(ObjectPath path, PrimitiveEditData editData) {
        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataSetPrimitiveC2SPacket(this.target, path, editData));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setNbtCompoundAt(ObjectPath path, NbtCompound tag) {
        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataSetNbtCompoundC2SPacket(this.target, path, tag));

        return CompletableFuture.completedFuture(null);
    }

    public void acceptPacket(FieldDataResponseS2CPacket packet) {
        var future = pendingFieldsRequests.get(packet.path());

        if (future == null) {
            Gadget.LOGGER.error("FieldDataResponseS2CPacket received with unknown ObjectPath {}", packet.path());
            return;
        }

        future.complete(packet.fields());
    }
}
