package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.dump.DumpedPacket;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SearchTextPacketHandler {
    Event<SearchTextPacketHandler> EVENT = EventFactory.createArrayBacked(SearchTextPacketHandler.class, callbacks -> (packet, searchText) -> {
        packet.searchTextErrors().clear();

        for (var callback : callbacks) {
            try (var reset = NetworkUtil.resetIndexes(packet.packet())) {
                callback.onCreateSearchText(packet, searchText);
            } catch (Exception e) {
                packet.searchTextErrors().add(e);
            }
        }
    });

    void onCreateSearchText(DumpedPacket packet, StringBuilder searchText);
}