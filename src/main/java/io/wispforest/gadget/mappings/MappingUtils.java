package io.wispforest.gadget.mappings;

import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.tinyremapper.IMappingProvider;

import java.util.HashMap;
import java.util.Map;

public final class MappingUtils {
    private MappingUtils() {

    }

    public static Map<String, String> createFieldIdUnmap(MappingTreeView tree, String to) {
        Map<String, String> map = new HashMap<>();

        for (var klass : tree.getClasses()) {
            String fromName = klass.getName("intermediary");
            if (fromName == null) fromName = klass.getSrcName();
            fromName = fromName.replace('/', '.');

            for (var field : klass.getFields()) {
                String intermediary =
                    fromName
                        + "#"
                        + field.getName("intermediary");
                String local =
                    klass.getName(to).replace('/', '.')
                        + "#"
                        + field.getName(to);

                map.put(local, intermediary);
            }
        }

        return map;
    }

    public static void feedMappings(IMappingProvider.MappingAcceptor acceptor, MappingTreeView tree, String from,
                                    String to) {
        for (var c : tree.getClasses()) {
            if (c.getName(from) == null) continue;

            String cTo = c.getName(to);

            if (cTo != null) acceptor.acceptClass(c.getName(from), cTo);

            for (var f : c.getFields()) {
                if (f.getName(from) == null) continue;

                var member = new IMappingProvider.Member(
                    c.getName(from),
                    f.getName(from),
                    f.getDesc(from)
                );

                String fTo = f.getName(to);

                if (fTo != null) acceptor.acceptField(member, fTo);
            }

            for (var m : c.getMethods()) {
                if (m.getName(from) == null) continue;

                var member = new IMappingProvider.Member(
                    c.getName(from),
                    m.getName(from),
                    m.getDesc(from)
                );

                String mTo = m.getName(to);

                if (mTo != null) acceptor.acceptMethod(member, mTo);
            }
        }
    }
}
