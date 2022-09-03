package me.basiqueevangelist.gadget.client.gui;

import me.basiqueevangelist.gadget.network.EntityDataS2CPacket;
import me.basiqueevangelist.gadget.network.GadgetNetworking;
import me.basiqueevangelist.gadget.network.RequestEntityDataC2SPacket;
import me.basiqueevangelist.gadget.path.ObjectPath;

public class EntityDataScreen extends BaseDataScreen {
    private final int networkId;

    public EntityDataScreen(EntityDataS2CPacket data) {
        this.networkId = data.networkId();

        data.fields().forEach((k, v) -> {
            fields.put(k, new ClientFieldData(v));
        });
    }

    @Override
    protected void requestPath(ObjectPath path) {
        GadgetNetworking.CHANNEL.clientHandle().send(new RequestEntityDataC2SPacket(this.networkId, path));
    }

    public void applyData(EntityDataS2CPacket packet) {
        packet.fields().forEach(this::addFieldData);
    }
}
