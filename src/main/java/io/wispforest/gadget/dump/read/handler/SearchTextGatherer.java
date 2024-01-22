package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface SearchTextGatherer {
    Event<SearchTextGatherer> EVENT = EventFactory.createArrayBacked(SearchTextGatherer.class, callbacks -> (packet, searchText, errSink) -> {
        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet)) {
                callback.gatherSearchText(packet, searchText, errSink);
            } catch (Exception e) {
                errSink.accept(e);
            }
        }
    });

    void gatherSearchText(DumpedPacket packet, StringBuilder out, ErrorSink errSink);
}