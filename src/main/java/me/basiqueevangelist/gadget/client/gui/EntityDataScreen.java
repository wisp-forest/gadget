package me.basiqueevangelist.gadget.client.gui;

import me.basiqueevangelist.gadget.desc.FieldObjects;
import me.basiqueevangelist.gadget.network.EntityDataS2CPacket;
import me.basiqueevangelist.gadget.network.GadgetNetworking;
import me.basiqueevangelist.gadget.network.RequestEntityDataC2SPacket;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.entity.Entity;

public class EntityDataScreen extends BaseDataScreen {
    private final int networkId;
    private final Entity clientEntity;

    public EntityDataScreen(EntityDataS2CPacket data) {
        this.networkId = data.networkId();
        this.clientEntity = null;

        data.fields().forEach(
            (k, v) -> fields.put(k, new ClientFieldData(v)));
    }

    public EntityDataScreen(Entity entity) {
        this.networkId = entity.getId();
        this.clientEntity = entity;

        FieldObjects.collectAllData(ObjectPath.EMPTY, clientEntity)
            .forEach((k, v) -> fields.put(k, new ClientFieldData(v)));
    }

    @Override
    public boolean isClient() {
        return clientEntity != null;
    }

    @Override
    protected void switchToClient() {
        Entity entity = client.world.getEntityById(networkId);

        if (entity == null) {
            return;
        }

        client.setScreen(new EntityDataScreen(entity));
    }

    @Override
    protected void switchToServer() {
        GadgetNetworking.CHANNEL.clientHandle().send(new RequestEntityDataC2SPacket(networkId, ObjectPath.EMPTY));
    }

    @Override
    protected void requestPath(ObjectPath path) {
        if (clientEntity == null) {
            GadgetNetworking.CHANNEL.clientHandle().send(new RequestEntityDataC2SPacket(this.networkId, path));
        } else {
            Object sub = path.follow(clientEntity);

            FieldObjects.collectAllData(path, sub).forEach(this::addFieldData);
        }
    }

    public void applyData(EntityDataS2CPacket packet) {
        packet.fields().forEach(this::addFieldData);
    }
}
