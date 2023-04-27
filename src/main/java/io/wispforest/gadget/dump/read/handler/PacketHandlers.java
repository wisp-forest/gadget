package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.dump.handler.MinecraftSupport;
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

        SearchTextGatherer.EVENT.addPhaseOrdering(FIRST_PHASE, Event.DEFAULT_PHASE);
        SearchTextGatherer.EVENT.register(FIRST_PHASE, (packet, searchText, errSink) -> {
            searchText.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

            if (packet.channelId() != null)
                searchText.append(" ").append(packet.channelId());
        });
    }
}
