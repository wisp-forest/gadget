package io.wispforest.gadget.dump.read;

import io.wispforest.gadget.util.ContextData;

public final class DumpReaderContext {
    public static final ContextData.NullableKey<DumpReaderContext> KEY = new ContextData.NullableKey<>();

    private final PacketDumpReader reader;

    DumpReaderContext(PacketDumpReader reader) {
        this.reader = reader;
    }

    public PacketDumpReader reader() {
        return reader;
    }
}
