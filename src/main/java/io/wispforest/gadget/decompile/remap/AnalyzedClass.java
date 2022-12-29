package io.wispforest.gadget.decompile.remap;

import java.util.List;

public record AnalyzedClass(String name, AnalyzedClass superclass, List<AnalyzedClass> interfaces,
                            List<MemberData> declaredFields, List<MemberData> declaredMethods) {
    public List<MemberData> declaredMembers(MemberType type) {
        return switch (type) {
            case METHOD -> declaredMethods;
            case FIELD -> declaredFields;
        };
    }

    public MemberData member(MemberType type, String fName, String fDesc) {
        for (var id : declaredMembers(type)) {
            if (id.name().equals(fName) && id.desc().equals(fDesc)) {
                return id;
            }
        }

        return openMember(type, fName, fDesc);
    }

    public MemberData openMember(MemberType type, String fName, String fDesc) {
        for (var id : declaredMembers(type)) {
            if (id.isPrivate()
                && id.name().equals(fName)
                && id.desc().equals(fDesc)) {
                return id;
            }
        }

        if (superclass != null) {
            MemberData member = superclass.openMember(type, fName, fDesc);

            if (member != null)
                return member;
        }

        for (var iface : interfaces) {
            MemberData member = iface.openMember(type, fName, fDesc);

            if (member != null)
                return member;
        }

        return null;
    }
}
