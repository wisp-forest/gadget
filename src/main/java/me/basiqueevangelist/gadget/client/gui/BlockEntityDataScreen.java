package me.basiqueevangelist.gadget.client.gui;

import me.basiqueevangelist.gadget.network.BlockEntityDataS2CPacket;
import me.basiqueevangelist.gadget.network.GadgetNetworking;
import me.basiqueevangelist.gadget.network.RequestBlockEntityDataC2SPacket;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.util.math.BlockPos;

public class BlockEntityDataScreen extends BaseDataScreen {
    private final BlockPos blockPos;

    public BlockEntityDataScreen(BlockEntityDataS2CPacket data) {
        this.blockPos = data.pos();

        data.fields().forEach((k, v) -> {
            fields.put(k, new ClientFieldData(v));
        });
    }

    @Override
    protected void requestPath(ObjectPath path) {
        GadgetNetworking.CHANNEL.clientHandle().send(new RequestBlockEntityDataC2SPacket(this.blockPos, path));
    }

    public void applyData(BlockEntityDataS2CPacket packet) {
        packet.fields().forEach(this::addFieldData);
    }
}
