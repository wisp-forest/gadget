package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.client.field.FieldDataIsland;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

public final class DrawPacketHandlers {
    public static final Identifier LAST_PHASE = Gadget.id("last");


    private DrawPacketHandlers() {

    }

    public static void init() {
        OwoSupport.init();
        FapiSupport.init();
        MinecraftSupport.init();

        DrawPacketHandler.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST_PHASE);
        DrawPacketHandler.EVENT.register(LAST_PHASE, (packet, view) -> {
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
