package me.basiqueevangelist.gadget.client.gui;

import me.basiqueevangelist.gadget.desc.FieldObjects;
import me.basiqueevangelist.gadget.network.BlockEntityDataS2CPacket;
import me.basiqueevangelist.gadget.network.GadgetNetworking;
import me.basiqueevangelist.gadget.network.RequestBlockEntityDataC2SPacket;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class BlockEntityDataScreen extends BaseDataScreen {
    private final BlockPos blockPos;
    private final BlockEntity clientBlockEntity;

    public BlockEntityDataScreen(BlockEntityDataS2CPacket data) {
        this.blockPos = data.pos();
        this.clientBlockEntity = null;

        data.fields().forEach(
            (k, v) -> fields.put(k, new ClientFieldData(v)));
    }

    public BlockEntityDataScreen(BlockEntity blockEntity) {
        this.blockPos = blockEntity.getPos();
        this.clientBlockEntity = blockEntity;

        FieldObjects.collectAllData(ObjectPath.EMPTY, blockEntity)
            .forEach((k, v) -> fields.put(k, new ClientFieldData(v)));
    }

    @Override
    public boolean isClient() {
        return clientBlockEntity != null;
    }

    @Override
    protected void switchToClient() {
        BlockEntity blockEntity = client.world.getBlockEntity(blockPos);

        if (blockEntity == null) {
            return;
        }

        client.setScreen(new BlockEntityDataScreen(blockEntity));
    }

    @Override
    protected void switchToServer() {
        GadgetNetworking.CHANNEL.clientHandle().send(new RequestBlockEntityDataC2SPacket(blockPos, ObjectPath.EMPTY));
    }

    @Override
    protected void requestPath(ObjectPath path) {
        if (clientBlockEntity == null) {
            GadgetNetworking.CHANNEL.clientHandle().send(new RequestBlockEntityDataC2SPacket(this.blockPos, path));
        } else {
            Object sub = path.follow(clientBlockEntity);

            FieldObjects.collectAllData(path, sub).forEach(this::addFieldData);
        }
    }

    public void applyData(BlockEntityDataS2CPacket packet) {
        packet.fields().forEach(this::addFieldData);
    }
}
