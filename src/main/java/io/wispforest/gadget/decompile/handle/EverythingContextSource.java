package io.wispforest.gadget.decompile.handle;

import org.jetbrains.java.decompiler.main.extern.IContextSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class EverythingContextSource implements IContextSource {
    private final QuiltflowerHandlerImpl handler;

    public EverythingContextSource(QuiltflowerHandlerImpl handler) {
        this.handler = handler;
    }

    @Override
    public String getName() {
        return "Everything";
    }

    @Override
    public Entries getEntries() {
        var klasses = new ArrayList<Entry>();
        for (var klass : handler.allUnmappedClasses()) {
            klasses.add(Entry.parse(handler.mapClass(klass)));
        }

        return new Entries(klasses, List.of(), List.of());
    }

    @Override
    public InputStream getInputStream(String resource) {
        var bytes = handler.getClassBytes(resource.replace(".class", ""));

        return new ByteArrayInputStream(bytes);
    }
}
