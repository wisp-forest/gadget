package io.wispforest.gadget.shell;

import jdk.jshell.execution.LoaderDelegate;
import jdk.jshell.spi.ExecutionControl;
import net.auoeke.reflect.Reflect;

import java.io.File;
import java.lang.instrument.ClassDefinition;
import java.net.URL;
import java.net.URLClassLoader;

public class ShellLoaderDelegate implements LoaderDelegate {
    private final Loader loader = new Loader();

    @Override
    public void load(ExecutionControl.ClassBytecodes[] cbcs) throws ExecutionControl.ClassInstallException, ExecutionControl.NotImplementedException, ExecutionControl.EngineTerminationException {
        boolean[] loaded = new boolean[cbcs.length];

        for (int i = 0; i < cbcs.length; i++) {
            try {
                loader.defineClass(cbcs[i].name(), cbcs[i].bytecodes());
                loaded[i] = true;
            } catch (Throwable error) {
                var cie = new ExecutionControl.ClassInstallException("Couldn't load " + cbcs[i].name(), loaded);
                cie.addSuppressed(error);
                throw cie;
            }
        }
    }

    @Override
    public void classesRedefined(ExecutionControl.ClassBytecodes[] cbcs) {
        try {
            var instrumentation = Reflect.instrument().value();

            boolean[] loaded = new boolean[cbcs.length];

            ClassDefinition[] defs = new ClassDefinition[cbcs.length];

            for (int i = 0; i < cbcs.length; i++) {
                defs[i] = new ClassDefinition(loader.loadClass(cbcs[i].name()), cbcs[i].bytecodes());
            }

            instrumentation.redefineClasses(defs);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't redefine classes", e);
        }
    }

    @Override
    public void addToClasspath(String classpath) throws ExecutionControl.InternalException {
        try {
            for (String path : classpath.split(File.pathSeparator)) {
                loader.addURL(new File(path).toURI().toURL());
            }
        } catch (Exception ex) {
            throw new ExecutionControl.InternalException(ex.toString());
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return loader.loadClass(name);
    }

    private static class Loader extends URLClassLoader {
        public Loader() {
            super(new URL[0], ShellLoaderDelegate.class.getClassLoader());
        }

        @Override
        public void addURL(URL url) {
            super.addURL(url);
        }

        private void defineClass(String name, byte[] bytes) {
            super.defineClass(name, bytes, 0, bytes.length);
        }

        static {
            ClassLoader.registerAsParallelCapable();
        }
    }
}
