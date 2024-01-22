package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.dump.read.unwrapped.UnwrappedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

// This is a quick and easy way to make generic packet-unwrapping gadget compat.
// Search text and packet component will be automatically provided by gadget.
public interface PacketUnwrapper {
    Event<PacketUnwrapper> EVENT = EventFactory.createArrayBacked(PacketUnwrapper.class, callbacks -> (packet, errSink) -> {
        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet)) {
                var unwrapped = callback.tryUnwrap(packet, errSink);

                if (unwrapped != null)
                    return unwrapped;
            } catch (Exception e) {
                errSink.accept(e);
            }
        }

        return null;
    });

    /**
     * Tries to unwrap this packet
     * @param packet the packet to unwrap
     * @param errSink error sink
     * @return An object that represents this packet. By default, this just makes gadget dump out packet fields, though
     * other interfaces can be implemented to add custom functionality
     */
    @Nullable UnwrappedPacket tryUnwrap(DumpedPacket packet, ErrorSink errSink);
}
