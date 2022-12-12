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
    public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
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
        return new SeekableInMemoryByteChannel(
            ((ClassesFileSystem) path.getFileSystem()).getBytes(((ClassesPath) path).path));
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter<? super Path> filter) throws IOException {
        if (!dir.isAbsolute()) throw new UnsupportedOperationException("bruh");

        String startPath = ((ClassesPath) dir).path.substring(1);
        if (!startPath.endsWith("/"))
            startPath = startPath + "/";

        String finalStartPath = startPath;
        ClassesFileSystem fs = (ClassesFileSystem) dir.getFileSystem();
        var classes = fs.getAllClasses()
            .stream()
            .filter(x -> x.startsWith(finalStartPath))
            .map(x -> x.substring(x.indexOf('/', finalStartPath.length())))
            .distinct()
            .map(x -> new ClassesPath(fs, "/" + x))
            .map(x -> (Path) x)
            .toList();

        return new DirectoryStream<>() {
            @Override
            public Iterator<Path> iterator() {
                return classes.iterator();
            }

            @Override
            public void close() throws IOException {

            }
        };
    }

    @Override
    public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void delete(Path path) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void copy(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void move(Path source, Path target, CopyOption... options) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public boolean isSameFile(Path path, Path path2) throws IOException {
        return path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) throws IOException {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
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

        boolean isRegularFile = false;
        boolean isDirectory = false;

        if (rawPath == "/") {
            isDirectory = true;
        } else if (rawPath.endsWith(".class"))
            isRegularFile = fs.getAllClasses().contains(rawPath.replace(".class", ""));
        else {
            String proper = (rawPath + "/").replace("//", "/");
            isDirectory = fs
                .getAllClasses()
                .stream()
                .anyMatch(x -> x.startsWith(proper));
        }

        if (!isRegularFile && !isDirectory)
            throw new FileNotFoundException("bruh");

        boolean finalIsDirectory = isDirectory;
        boolean finalIsRegularFile = isRegularFile;
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
                return finalIsRegularFile;
            }

            @Override
            public boolean isDirectory() {
                return finalIsDirectory;
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
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }

    @Override
    public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
        throw new UnsupportedOperationException("bruh");
    }
}
