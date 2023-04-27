package io.wispforest.gadget.dump.read.handler;

import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.util.ErrorSink;
import io.wispforest.gadget.util.NetworkUtil;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

// This is a quick and easy way to make generic packet-unwrapping gadget compat.
// Search text and packet component will be automatically provided by gadget.
public interface PacketUnwrapper {
    Event<PacketUnwrapper> EVENT = EventFactory.createArrayBacked(PacketUnwrapper.class, callbacks -> (packet, errSink) -> {
        for (var callback : callbacks) {
            try (var ignored = NetworkUtil.resetIndexes(packet.packet())) {
                var unwrapped = callback.tryUnwrap(packet, errSink);

                if (unwrapped != null)
                    return unwrapped;
            } catch (Exception e) {
                errSink.accept(e);
            }
        }

        return null;
    });

    @Nullable Unwrapped tryUnwrap(DumpedPacket packet, ErrorSink errSink);

    record Unwrapped(Object packet, OptionalInt packetId) {
        public Unwrapped(Object packet) {
            this(packet, OptionalInt.empty());
        }

        public Unwrapped(Object packet, int packetId) {
            this(packet, OptionalInt.of(packetId));
        }
    }
}
