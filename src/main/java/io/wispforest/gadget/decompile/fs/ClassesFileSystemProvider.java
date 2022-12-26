package io.wispforest.gadget.decompile.fs;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class ClassesFileSystemProvider extends FileSystemProvider {
    public static final FileSystemProvider INSTANCE = new ClassesFileSystemProvider();

    private ClassesFileSystemProvider() {

    }

    @Override
    public String getScheme() {
        return "gadget_classes";
    }

    @Override
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
        throw new RuntimeException("mald");
    }

    @Override
    public FileSystem getFileSystem(URI uri) {
        throw new RuntimeException("mald");
    }

    @NotNull
    @Override
    public Path getPath(@NotNull URI uri) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {
        String rawPath = ((ClassesPath) path).path;

        if (rawPath.startsWith("/"))
            rawPath = rawPath.substring(1);

        byte[] data = ((ClassesFileSystem) path.getFileSystem()).getBytes(rawPath);
        if (data == null)
            throw new FileNotFoundException();
        return new SeekableInMemoryByteChannel(data);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) {
        if (!dir.isAbsolute()) throw new UnsupportedOperationException("bruh");

        String startPath = ((ClassesPath) dir).path.substring(1);
        if (startPath.endsWith("/"))
            startPath = startPath.substring(0, startPath.length() - 1);

        ClassesFileSystem fs = (ClassesFileSystem) dir.getFileSystem();

        ClassesFileSystem.TreeElement el = fs.getTreeRoot().follow(startPath);
        String finalStartPath = startPath;
        List<Path> children = el.children
            .stream()
            .map(x -> new ClassesPath(fs, ("/" + finalStartPath + "/" + x.name).replace("//", "/")))
            .map(x -> (Path) x)
            .toList();

        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                return children.iterator();
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void delete(Path path) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) {
        return path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void checkAccess(Path path, AccessMode... modes) throws IOException {
        if (ArrayUtils.contains(modes, AccessMode.WRITE))
            throw new IOException("bruh");
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
        throw new UnsupportedOperationException("bruh");
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options) throws IOException {
        if (type != BasicFileAttributes.class) throw new UnsupportedOperationException("bruh");

        ClassesFileSystem fs = (ClassesFileSystem) path.getFileSystem();
        String rawPath = ((ClassesPath) path).path;

        var el = fs.getTreeRoot().follow(rawPath);

        if (el == null)
            throw new FileNotFoundException();

        boolean isFile = el.name.endsWith(".class");

        return (A) new BasicFileAttributes() {

            @Override
            public FileTime lastModifiedTime() {
                return FileTime.fromMillis(0);
            }

            @Override
            public FileTime lastAccessTime() {
                return FileTime.fromMillis(0);
            }

            @Override
            public FileTime creationTime() {
                return FileTime.fromMillis(0);
            }

            @Override
            public boolean isRegularFile() {
                return isFile;
            }

            @Override
            public boolean isDirectory() {
                return !isFile;
            }

            @Override
            public boolean isSymbolicLink() {
                return false;
            }

            @Override
            public boolean isOther() {
                return false;
            }

            @Override
            public long size() {
                return 0;
            }

            @Override
            public Object fileKey() {
                return null;
            }
        };
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) {
        throw new UnsupportedOperationException("bruh");
    }
}
