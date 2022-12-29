package io.wispforest.gadget.decompile.remap;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class ClassAnalyzer extends ClassVisitor {
    private String name;
    private AnalyzedClass superclass;
    private final List<AnalyzedClass> interfaces = new ArrayList<>();
    private final List<MemberData> declaredFields = new ArrayList<>();
    private final List<MemberData> declaredMethods = new ArrayList<>();
    private final RemapperStore store;

    public ClassAnalyzer(RemapperStore store) {
        super(Opcodes.ASM9);
        this.store = store;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.name = name;

        if (superName != null)
            this.superclass = store.getClass(superName);

        for (var iface : interfaces) {
            var c = store.getClass(iface);

            if (c != null)
                this.interfaces.add(c);
        }
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        boolean isPrivate = (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0;
        declaredFields.add(new MemberData(MemberType.FIELD, this.name, name, descriptor, isPrivate));
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        boolean isPrivate = (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0;
        declaredMethods.add(new MemberData(MemberType.METHOD, this.name, name, descriptor, isPrivate));
        return null;
    }

    public AnalyzedClass build() {
        return new AnalyzedClass(name, superclass, interfaces, declaredFields, declaredMethods);
    }
}
