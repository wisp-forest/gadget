package io.wispforest.gadget.decompile.fs;

import io.wispforest.gadget.decompile.KnotUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;

public class ClassesFileSystem extends FileSystem {
    private final Map<String, byte[]> classBytecodeStash = new HashMap<>();
    private final List<String> classNames;
    private TreeElement root;

    public ClassesFileSystem() {
        classNames = new ArrayList<>();

        for (Class<?> klass : KnotUtil.INSTRUMENTATION.getInitiatedClasses(ClassesFileSystem.class.getClassLoader())) {
            if (klass.isHidden()) continue;
            if (klass.isArray()) continue;

            classNames.add(klass.getName().replace('.', '/'));
        }
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
        if (path.endsWith(".class"))
            path = path.substring(0, path.length() - 6);

        return classBytecodeStash.computeIfAbsent(path.replace('/', '.'),
            name2 -> KnotUtil.getPostMixinClassByteArray(name2, true));
    }

    public List<String> getAllClasses() {
        return classNames;
    }

    public TreeElement getTreeRoot() {
        if (root != null) return root;

        root = new TreeElement();
        classNames.forEach(root::populatePath);

        return root;
    }

    public static class TreeElement {
        public String name = "";
        public List<TreeElement> children = new ArrayList<>();

        public void populatePath(String path) {
            TreeElement current = this;
            String[] elements = path.split("/");

            for (int i = 0; i < elements.length; i++) {
                String el = elements[i];
                if (el.equals("")) continue;

                boolean found = false;
                for (var possible : current.children) {
                    if (possible.name.equals(el)) {
                        found = true;
                        current = possible;
                    }
                }

                if (!found) {
                    var child = new TreeElement();
                    child.name = el + (i == elements.length - 1 ? ".class" : "");
                    current.children.add(child);
                    current = child;
                }
            }
        }

        public TreeElement follow(String path) {
            TreeElement current = this;
            String[] elements = path.split("/");

            for (String el : elements) {
                if (el.equals("")) continue;

                for (var treeEl : current.children) {
                    if (Objects.equals(treeEl.name, el)) {
                        current = treeEl;
                        break;
                    }
                }
            }

            return current;
        }
    }
}
