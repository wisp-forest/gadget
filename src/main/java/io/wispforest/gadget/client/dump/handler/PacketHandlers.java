package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.util.ReflectionUtil;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

public final class PacketHandlers {
    public static final Identifier LAST_PHASE = Gadget.id("last");


    private PacketHandlers() {

    }

    public static void init() {
        OwoSupport.init();
        FapiSupport.init();
        MinecraftSupport.init();

        ProcessPacketHandler.EVENT.register((packet, view, searchText) -> {
            searchText.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

            if (packet.channelId() != null)
                searchText.append(" ").append(packet.channelId());

            return false;
        });

        ProcessPacketHandler.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST_PHASE);
        ProcessPacketHandler.EVENT.register(LAST_PHASE, (packet, view, searchText) -> {
            if (packet.channelId() != null) {
                view.child(GuiUtil.hexDump(NetworkUtil.unwrapCustom(packet.packet())));
                return true;
            }

            FieldDataIsland island = new FieldDataIsland();
            island.shortenNames();
            island.targetObject(packet.packet(), false);

            view.child(island.mainContainer());
            return true;
        });
    }
}
