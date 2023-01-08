package io.wispforest.gadget.decompile.handle;

import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ClassContextSource implements IContextSource {
    private final QuiltflowerHandlerImpl handler;
    private final Class<?>[] classes;

    public ClassContextSource(QuiltflowerHandlerImpl handler, Class<?> klass) {
        this.handler = handler;

        classes = klass.getNestHost().getNestMembers();
    }

    @Override
    public String getName() {
        return "Class";
    }

    @Override
    public Entries getEntries() {
        var klasses = new ArrayList<Entry>();
        for (var klass : classes) {
            klasses.add(Entry.parse(handler.mapClass(klass.getName().replace('.', '/'))));
        }

        return new Entries(klasses, List.of(), List.of());
    }

    @Override
    public InputStream getInputStream(String resource) {
        var bytes = handler.getClassBytes(handler.mapClass(resource.replace(".class", "")));

        return new ByteArrayInputStream(bytes);
    }

    @Override
    public IOutputSink createOutputSink(IResultSaver saver) {
        return new GadgetOutputSink((GadgetResultSaver) saver);
    }
}
