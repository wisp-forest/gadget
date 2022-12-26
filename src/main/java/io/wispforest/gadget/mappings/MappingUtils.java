package io.wispforest.gadget.mappings;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ProgressToast;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.tinyremapper.IMappingProvider;
import org.apache.commons.lang3.mutable.MutableInt;

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

    private static String tryGetName(MappingTreeView.ElementMappingView view, String... froms) {
        for (String from : froms) {
            String text = view.getName(from);

            if (text != null)
                return text;
        }

        return null;
    }

    private static String tryGetDesc(MappingTreeView.MemberMappingView view, String... froms) {
        return view.getDesc(froms[0]);
    }

    public static void feedMappings(IMappingProvider.MappingAcceptor acceptor, MappingTreeView tree,
                                    ProgressToast toast, String to, String... froms) {
        int elements = 0;
        for (var c : tree.getClasses()) {
            elements += 1 + c.getFields().size() + c.getMethods().size();
        }

        MutableInt progress = new MutableInt(0);
        toast.followProgress(progress::getValue, elements);

        if (Gadget.CONFIG.internalSettings.dumpTRMappings())
            acceptor = new SavingMappingAcceptor(acceptor);

        for (var c : tree.getClasses()) {
            String cFrom = tryGetName(c, froms);
            if (cFrom == null) continue;

            String cTo = c.getName(to);

            if (cTo != null) acceptor.acceptClass(cFrom, cTo);

            for (var f : c.getFields()) {
                String fFrom = tryGetName(f, froms);
                if (fFrom == null) continue;

                var member = new IMappingProvider.Member(
                    cFrom,
                    fFrom,
                    tryGetDesc(f, froms)
                );

                String fTo = f.getName(to);

                if (fTo != null) acceptor.acceptField(member, fTo);

                progress.add(1);
            }

            for (var m : c.getMethods()) {
                String mFrom = tryGetName(m, froms);
                if (mFrom == null) continue;

                var member = new IMappingProvider.Member(
                    cFrom,
                    mFrom,
                    tryGetDesc(m, froms)
                );

                String mTo = m.getName(to);

                if (mTo != null) acceptor.acceptMethod(member, mTo);

                progress.add(1);
            }

            progress.add(1);
        }
    }
}
