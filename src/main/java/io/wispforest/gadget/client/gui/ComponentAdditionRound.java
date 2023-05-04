package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class ComponentAdditionRound {
    private final Map<FlowLayout, ComponentData> data = new LinkedHashMap<>();

    public void addTo(FlowLayout parent, Component child) {
        data.computeIfAbsent(parent, unused -> new ComponentData()).newChildren.add(child);
    }

    public void removeFrom(FlowLayout parent, Component child) {
        data.computeIfAbsent(parent, unused -> new ComponentData()).removedChildren.add(child);
    }

    public void commit() {
        for (var entry : data.entrySet()) {
            entry.getKey().<FlowLayout>configure(c -> {
                for (var removed : entry.getValue().removedChildren)
                    c.removeChild(removed);

                c.children(entry.getValue().newChildren);
            });
        }
    }

    private static class ComponentData {
        private final Collection<Component> newChildren = new LinkedHashSet<>();
        private final Collection<Component> removedChildren = new LinkedHashSet<>();
    }
}
