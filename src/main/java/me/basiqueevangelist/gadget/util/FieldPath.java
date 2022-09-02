package me.basiqueevangelist.gadget.util;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public record FieldPath(List<String> names) implements Comparable<FieldPath> {
    public Object follow(Object o) {
        for (String name : names) {
            if (o.getClass().isArray()) {
                o = Array.get(o, Integer.parseInt(name));
            } else {
                try {
                    var field = ReflectionUtil.findField(o.getClass(), name);

                    if (!field.canAccess(o))
                        field.setAccessible(true);

                    o = field.get(o);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return o;
    }

    public String name() {
        return names.get(names.size() - 1);
    }

    public FieldPath parent() {
        List<String> newList = new ArrayList<>(names);

        newList.remove(newList.size() - 1);

        return new FieldPath(newList);
    }

    public FieldPath then(String name) {
        List<String> newList = new ArrayList<>(names);

        newList.add(name);

        return new FieldPath(newList);
    }

    @Override
    public int compareTo(@NotNull FieldPath o) {
        for (int i = 0; i < o.names.size() && i < names.size(); i++) {
            int compared = names.get(i).compareTo(o.names.get(i));

            if (compared != 0)
                return compared;
        }

        return names.size() - o.names.size();
    }
}
