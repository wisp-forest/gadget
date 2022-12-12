package io.wispforest.gadget.decompile.fs;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;

public class ClassesPath implements Path {
    private final ClassesFileSystem fs;
    String path;

    public ClassesPath(ClassesFileSystem fs, String path) {
        this.fs = fs;
        this.path = path;
    }

    @NotNull
    @Override
    public FileSystem getFileSystem() {
        return fs;
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith("/");
    }

    @Override
    public Path getRoot() {
        return new ClassesPath(fs, "/");
    }

    @Override
    public Path getFileName() {
        return new ClassesPath(fs, path.substring(path.lastIndexOf('/') + 1));
    }

    @Override
    public Path getParent() {
        return new ClassesPath(fs, path.substring(path.lastIndexOf('/')));
    }

    @Override
    public int getNameCount() {
        return path.split("/").length;
    }

    @NotNull
    @Override
    public Path getName(int index) {
        var split = path.split("/");

        if (split[0].equals(""))
            return new ClassesPath(fs, split[index + 1]);
        else
            return new ClassesPath(fs, split[index]);
    }

    @NotNull
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @Override
    public boolean startsWith(@NotNull Path other) {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @Override
    public boolean endsWith(@NotNull Path other) {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public Path normalize() {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public Path resolve(@NotNull Path other) {
        return new ClassesPath(fs, (path + ((ClassesPath) other).path).replace("//", "/"));
    }

    @NotNull
    @Override
    public Path relativize(@NotNull Path other) {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public URI toUri() {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public Path toAbsolutePath() {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public Path toRealPath(@NotNull LinkOption @NotNull ... options) {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @NotNull
    @Override
    public WatchKey register(@NotNull WatchService watcher, @NotNull WatchEvent.Kind<?> @NotNull [] events, WatchEvent.Modifier... modifiers) throws IOException {
        throw new UnsupportedOperationException("can't be bothered to impl this");
    }

    @Override
    public int compareTo(@NotNull Path other) {
        return path.compareTo(((ClassesPath) other).path);
    }

    @Override
    public String toString() {
        return path;
    }
}
