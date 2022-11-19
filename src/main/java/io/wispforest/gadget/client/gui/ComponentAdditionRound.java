package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;

import java.util.*;

public class ComponentAdditionRound {
    private final Map<FlowLayout, Collection<Component>> newComponents = new LinkedHashMap<>();

    public void addTo(FlowLayout parent, Component child) {
        newComponents.computeIfAbsent(parent, unused -> new LinkedHashSet<>()).add(child);
    }

    public void commit() {
        for (var entry : newComponents.entrySet()) {
            entry.getKey().children(entry.getValue());
        }
    }
}
