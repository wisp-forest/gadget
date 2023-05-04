package io.wispforest.gadget.client.field;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.gadget.field.FieldDataSource;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.network.GadgetNetworking;
import io.wispforest.gadget.network.InspectionTarget;
import io.wispforest.gadget.network.packet.c2s.FieldDataRequestC2SPacket;
import io.wispforest.gadget.network.packet.c2s.FieldDataSetNbtCompoundC2SPacket;
import io.wispforest.gadget.network.packet.c2s.FieldDataSetPrimitiveC2SPacket;
import io.wispforest.gadget.network.packet.s2c.FieldDataErrorS2CPacket;
import io.wispforest.gadget.network.packet.s2c.FieldDataResponseS2CPacket;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import io.wispforest.gadget.util.CancellationTokenSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class RemoteFieldDataSource implements FieldDataSource, AutoCloseable {
    private final InspectionTarget target;
    private final FieldData rootData;
    private final Map<PathStep, FieldData> rootFields;

    private final CancellationTokenSource cancelSource = new CancellationTokenSource();
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
        cancelSource.token().throwIfCancelled();

        if (pendingFieldsRequests.containsKey(path)) return pendingFieldsRequests.get(path);

        CompletableFuture<Map<PathStep, FieldData>> future = new CompletableFuture<>();

        pendingFieldsRequests.put(path, future);

        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataRequestC2SPacket(this.target, path, from, limit));

        if (Gadget.CONFIG.internalSettings.dumpFieldDataRequests())
            Gadget.LOGGER.info("-> {}", path);

        return cancelSource.token().wrapFuture(future.orTimeout(10, TimeUnit.SECONDS));
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public CompletableFuture<Void> setPrimitiveAt(ObjectPath path, PrimitiveEditData editData) {
        cancelSource.token().throwIfCancelled();

        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataSetPrimitiveC2SPacket(this.target, path, editData));

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> setNbtCompoundAt(ObjectPath path, NbtCompound tag) {
        cancelSource.token().throwIfCancelled();

        GadgetNetworking.CHANNEL.clientHandle().send(new FieldDataSetNbtCompoundC2SPacket(this.target, path, tag));

        return CompletableFuture.completedFuture(null);
    }

    public void acceptPacket(FieldDataResponseS2CPacket packet) {
        var future = pendingFieldsRequests.remove(packet.path());

        if (Gadget.CONFIG.internalSettings.dumpFieldDataRequests())
            Gadget.LOGGER.info("<- {}", packet.path());

        if (future == null) {
            Gadget.LOGGER.error("FieldDataResponseS2CPacket received with unknown ObjectPath {}", packet.path());
            return;
        }

        future.complete(packet.fields());
    }

    public void acceptPacket(FieldDataErrorS2CPacket packet) {
        var future = pendingFieldsRequests.remove(packet.path());

        if (Gadget.CONFIG.internalSettings.dumpFieldDataRequests())
            Gadget.LOGGER.info("<- {} (err)", packet.path());

        if (future == null) {
            Gadget.LOGGER.error("FieldDataErrorS2CPacket received with unknown ObjectPath {}", packet.path());
            return;
        }

        future.completeExceptionally(new RemoteErrorException(packet.message()));
    }

    @Override
    public void close() {
        cancelSource.cancel();
    }

    public static class RemoteErrorException extends RuntimeException {
        private final Text message;

        public RemoteErrorException(Text message) {
            super(message.getString());
            this.message = message;
        }

        public Text message() {
            return message;
        }
    }
}
