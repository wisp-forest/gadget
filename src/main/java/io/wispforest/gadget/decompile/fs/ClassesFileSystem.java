package io.wispforest.gadget.decompile.fs;

import io.wispforest.gadget.decompile.KnotUtil;
import net.auoeke.reflect.Reflect;
import net.fabricmc.tinyremapper.api.TrRemapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class ClassesFileSystem extends FileSystem {
    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();
    private final List<String> classNames;
    private final TrRemapper remapper;

    public ClassesFileSystem(TrRemapper remapper) {
        classNames = Arrays.stream(Reflect.instrument()
                .valueOr(null)
                .getAllLoadedClasses())
            .filter(x -> !x.isHidden())
            .map(Class::getName)
            .map(remapper::map)
            .map(x -> x.replace('.', '/'))
            .toList();
        this.remapper = remapper;
    }

    @Override
    public FileSystemProvider provider() {
        return ClassesFileSystemProvider.INSTANCE;
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public String getSeparator() {
        return null;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return Collections.singletonList(new ClassesPath(this, "/"));
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return null;
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return null;
    }

    @NotNull
    @Override
    public Path getPath(@NotNull String first, @NotNull String @NotNull ... more) {
        return new ClassesPath(this, first + "/" + String.join("/", more));
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        return null;
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return null;
    }

    @Override
    public WatchService newWatchService() {
        return null;
    }

    public byte[] getBytes(String path) {
        return classBytecodeStash.computeIfAbsent(remapper.map(path
                .replace(".class", ""))
                .replace('/', '.'),
            name2 -> KnotUtil.getPostMixinClassByteArray(name2, true));
    }

    public List<String> getAllClasses() {
        return classNames;
    }
}
