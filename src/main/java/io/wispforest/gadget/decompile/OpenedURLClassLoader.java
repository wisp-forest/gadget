package io.wispforest.gadget.decompile;

import java.net.URL;
import java.net.URLClassLoader;

public class OpenedURLClassLoader extends URLClassLoader {
    private final String ownPrefix;

    public OpenedURLClassLoader(URL[] urls, ClassLoader parent, String ownPrefix) {
        super(urls, parent);
        this.ownPrefix = ownPrefix;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                if (!name.startsWith(ownPrefix)) {
                    try {
                        c = getParent().loadClass(name);
                    } catch (ClassNotFoundException e) {
                        // ...
                    }
                }

                if (c == null) {
                    c = findClass(name);
                }
            }

            if (resolve) {
                resolveClass(c);
            }

            return c;
        }
    }

    static {
        ClassLoader.registerAsParallelCapable();
    }
}
