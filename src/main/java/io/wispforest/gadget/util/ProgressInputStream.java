package io.wispforest.gadget.util;

import org.jetbrains.annotations.NotNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressInputStream extends FilterInputStream {
    private int progress = 0;
    private int markedProgress = 0;

    protected ProgressInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int data = super.read();
        if (data != -1) progress++;
        return data;
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        int read = super.read(b);
        progress += read;
        return read;
    }

    @Override
    public int read(byte @NotNull [] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        progress += read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        progress += skipped;
        return skipped;
    }

    @Override
    public synchronized void mark(int readlimit) {
        markedProgress = progress;
        super.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
        progress = markedProgress;
    }

    public int progress() {
        return progress;
    }
}
