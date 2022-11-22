package io.wispforest.gadget.network;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.desc.FieldObjects;
import io.wispforest.gadget.network.packet.c2s.*;
import io.wispforest.gadget.network.packet.s2c.AnnounceS2CPacket;
import io.wispforest.gadget.network.packet.s2c.DataS2CPacket;
import io.wispforest.gadget.network.packet.s2c.ResourceDataS2CPacket;
import io.wispforest.gadget.network.packet.s2c.ResourceListS2CPacket;
import io.wispforest.gadget.path.EnumMapPathStepType;
import io.wispforest.gadget.path.SimpleMapPathStepType;
import io.wispforest.owo.network.OwoNetChannel;
import io.wispforest.gadget.desc.edit.PrimitiveEditTypes;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.HashMap;

public final class GadgetNetworking {
    public static final OwoNetChannel CHANNEL = OwoNetChannel.createOptional(Gadget.id("data"));

    private GadgetNetworking() {

    }

    public static void init() {
        SimpleMapPathStepType.init();
        EnumMapPathStepType.init();
        PrimitiveEditTypes.init();

        CHANNEL.registerServerbound(RequestDataC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.inspect", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }


            Object target = packet.target().resolve(access.player().world);

            if (target == null) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.notfound"), true);
                return;
            }

            Object o = packet.path().follow(target);

            var fields = FieldObjects.collectAllData(packet.path(), o);

            CHANNEL.serverHandle(access.player()).send(new DataS2CPacket(packet.target(), fields));
        });

        CHANNEL.registerServerbound(SetPrimitiveC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.inspect", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }


            Object target = packet.target().resolve(access.player().world);

            if (target == null) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.notfound"), true);
                return;
            }

            packet.path().set(target, packet.data().toObject());

            var parentPath = packet.path().parent();

            Object o = parentPath.follow(target);

            var fields = FieldObjects.collectAllData(parentPath, o);

            CHANNEL.serverHandle(access.player()).send(new DataS2CPacket(packet.target(), fields));
        });

        CHANNEL.registerServerbound(SetNbtCompoundC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.inspect", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }

            Object target = packet.target().resolve(access.player().world);

            if (target == null) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.notfound"), true);
                return;
            }

            packet.path().set(target, packet.data());

            var parentPath = packet.path().parent();

            Object o = parentPath.follow(target);

            var fields = FieldObjects.collectAllData(parentPath, o);

            CHANNEL.serverHandle(access.player()).send(new DataS2CPacket(packet.target(), fields));
        });

        CHANNEL.registerServerbound(ReplaceStackC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.replaceStack", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"));
                return;
            }

            ScreenHandler screenHandler = access.player().currentScreenHandler;

            if (screenHandler == null)
                return;

            screenHandler.slots.get(packet.slotId()).setStack(packet.stack());
        });

        CHANNEL.registerServerbound(ListResourcesC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.requestServerData", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }

            var resources = access.runtime().getResourceManager().findAllResources("", x -> true);
            var network = new HashMap<Identifier, Integer>();

            for (var entry : resources.entrySet())
                network.put(entry.getKey(), entry.getValue().size());

            CHANNEL.serverHandle(access.player()).send(new ResourceListS2CPacket(network));
        });

        CHANNEL.registerServerbound(RequestResourceC2SPacket.class, (packet, access) -> {
            if (!Permissions.check(access.player(), "gadget.requestServerData", 4)) {
                access.player().sendMessage(Text.translatable("message.gadget.fail.permissions"), true);
                return;
            }

            var resources = access.runtime().getResourceManager().getAllResources(packet.id());

            try {
                CHANNEL.serverHandle(access.player()).send(
                    new ResourceDataS2CPacket(packet.id(), resources.get(packet.index()).getInputStream().readAllBytes()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        CHANNEL.registerClientboundDeferred(DataS2CPacket.class);
        CHANNEL.registerClientboundDeferred(AnnounceS2CPacket.class);
        CHANNEL.registerClientboundDeferred(ResourceListS2CPacket.class);
        CHANNEL.registerClientboundDeferred(ResourceDataS2CPacket.class);
    }
}
