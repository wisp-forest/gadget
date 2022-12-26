package io.wispforest.gadget.mappings;

import net.fabricmc.tinyremapper.IMappingProvider;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class SavingMappingAcceptor implements IMappingProvider.MappingAcceptor {
    private final IMappingProvider.MappingAcceptor wrapping;
    private final Writer writer;
    private String currentClass;
    private String currentMethod;

    public SavingMappingAcceptor(IMappingProvider.MappingAcceptor wrapping) {
        this.wrapping = wrapping;

        try {
            Files.createDirectories(Path.of("gadget/debug"));
            writer = Files.newBufferedWriter(Path.of("gadget/debug/tr_mappings.dump"),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptClass(String srcName, String dstName) {
        wrapping.acceptClass(srcName, dstName);

        currentClass = srcName;

        try {
            writer.write(srcName + " -> " + dstName + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptMethod(IMappingProvider.Member method, String dstName) {
        wrapping.acceptMethod(method, dstName);

        currentMethod = method.owner + "#" + method.name + method.desc;

        try {
            if (!Objects.equals(currentClass, method.owner)) {
                writer.write(method.owner + "#" + method.name + method.desc + " -> " + dstName + "\n");
            } else {
                writer.write("    " + method.name + method.desc + " -> " + dstName + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptMethodArg(IMappingProvider.Member method, int lvIndex, String dstName) {
        try {
            if (!Objects.equals(currentMethod, method.owner + "#" + method.name + method.desc)) {
                writer.write(method.owner + "#" + method.name + method.desc + "[" + lvIndex + "] -> " + dstName + "\n");
            } else {
                writer.write("        " + lvIndex + " -> " + dstName + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptMethodVar(IMappingProvider.Member method, int lvIndex, int startOpIdx, int asmIndex, String dstName) {
        try {
            if (!Objects.equals(currentMethod, method.owner + "#" + method.name + method.desc)) {
                writer.write(method.owner + "#" + method.name + method.desc + "[" + lvIndex + ":" + asmIndex + "] -> " + dstName + "\n");
            } else {
                writer.write("        " + lvIndex + ":" + asmIndex + " -> " + dstName + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void acceptField(IMappingProvider.Member field, String dstName) {
        wrapping.acceptField(field, dstName);

        try {
            if (!Objects.equals(currentClass, field.owner)) {
                writer.write(field.owner + "#" + field.name + ":" + field.desc + " -> " + dstName + "\n");
            } else {
                writer.write("    " + field.name + ":" + field.desc + " -> " + dstName + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
