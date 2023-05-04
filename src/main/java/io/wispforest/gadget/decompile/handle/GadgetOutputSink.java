package io.wispforest.gadget.decompile.handle;

import org.jetbrains.java.decompiler.main.extern.IContextSource;

public class GadgetOutputSink implements IContextSource.IOutputSink {
    private final GadgetResultSaver saver;

    public GadgetOutputSink(GadgetResultSaver saver) {

        this.saver = saver;
    }

    @Override
    public void begin() {

    }

    @Override
    public void acceptClass(String qualifiedName, String fileName, String content, int[] mapping) {
        saver.saved = content;
    }

    @Override
    public void acceptDirectory(String directory) {

    }

    @Override
    public void acceptOther(String path) {

    }

    @Override
    public void close() {

    }
}
