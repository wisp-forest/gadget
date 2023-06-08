package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.dump.read.handler.PacketUnwrapper;
import io.wispforest.gadget.dump.read.unwrapped.UnwrappedPacket;
import io.wispforest.gadget.util.ContextData;
import io.wispforest.gadget.util.ErrorSink;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public class UnwrappedPacketData {
    public static final ContextData.Key<DumpedPacket, UnwrappedPacketData> KEY = new ContextData.Key<>(UnwrappedPacketData::new);

    private final DumpedPacket packet;
    private SoftReference<UnwrappedPacket> unwrapped;
    private final List<Throwable> errors = new ArrayList<>();

    public UnwrappedPacketData(DumpedPacket packet) {
        this.packet = packet;
    }

    public UnwrappedPacket unwrap() {
        if (unwrapped == null || unwrapped.get() == null) {
            errors.clear();
            unwrapped = new SoftReference<>(PacketUnwrapper.EVENT.invoker().tryUnwrap(packet, errors::add));
        }

        return unwrapped.get();
    }

    public void copyErrors(ErrorSink errSink) {
        errors.forEach(errSink::accept);
    }

    public List<Throwable> errors() {
        return errors;
    }
}
