package me.basiqueevangelist.gadget.network;

import io.wispforest.owo.network.OwoNetChannel;
import me.basiqueevangelist.gadget.desc.FieldObjects;
import me.basiqueevangelist.gadget.path.MapPathStepType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import static me.basiqueevangelist.gadget.Gadget.id;

public final class GadgetNetworking {
    public static final OwoNetChannel CHANNEL = OwoNetChannel.createOptional(id("data"));

    private GadgetNetworking() {

    }

    public static void init() {
        MapPathStepType.init();

        CHANNEL.registerServerbound(RequestBlockEntityDataC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.inspect.blockentity", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }


            BlockEntity be = access.player().world.getBlockEntity(packet.pos());

            if (be == null) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.noblockentity"), true);
                return;
            }

            Object o = packet.path().follow(be);

            var fields = FieldObjects.collectAllData(packet.path(), o);

            CHANNEL.serverHandle(access.player()).send(new BlockEntityDataS2CPacket(packet.pos(), fields));
        });

        CHANNEL.registerServerbound(RequestEntityDataC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.inspect.entity", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }


            Entity e = access.player().world.getEntityById(packet.networkId());

            if (e == null) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.noentity"), true);
                return;
            }

            Object o = packet.path().follow(e);

            var fields = FieldObjects.collectAllData(packet.path(), o);

            CHANNEL.serverHandle(access.player()).send(new EntityDataS2CPacket(packet.networkId(), fields));
        });

        CHANNEL.registerClientboundDeferred(BlockEntityDataS2CPacket.class);
        CHANNEL.registerClientboundDeferred(EntityDataS2CPacket.class);
    }
}
