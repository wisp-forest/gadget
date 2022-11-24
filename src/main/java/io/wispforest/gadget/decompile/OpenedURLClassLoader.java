package io.wispforest.gadget.decompile;

import java.net.URL;
import java.net.URLClassLoader;

public class OpenedURLClassLoader extends URLClassLoader {
    public OpenedURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }
}
