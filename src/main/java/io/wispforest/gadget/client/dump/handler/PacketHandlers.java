package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.util.ReflectionUtil;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.util.Identifier;

public final class PacketHandlers {
    public static final Identifier FIRST_PHASE = Gadget.id("first");
    public static final Identifier LAST_PHASE = Gadget.id("last");


    private PacketHandlers() {

    }

    public static void init() {
        OwoSupport.init();
        FapiSupport.init();
        MinecraftSupport.init();

        SearchTextPacketHandler.EVENT.addPhaseOrdering(FIRST_PHASE, Event.DEFAULT_PHASE);
        SearchTextPacketHandler.EVENT.register(FIRST_PHASE, (packet, searchText) -> {
            searchText.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

            if (packet.channelId() != null)
                searchText.append(" ").append(packet.channelId());
        });

        DrawPacketHandler.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST_PHASE);
        DrawPacketHandler.EVENT.register(LAST_PHASE, (packet, view) -> {
            if (packet.channelId() != null) {
                var buf = NetworkUtil.unwrapCustom(packet.packet());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                view.child(GuiUtil.hexDump(bytes));
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
