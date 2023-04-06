package io.wispforest.gadget.field;

import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class FieldDataHolder<N extends FieldDataNode<N>> {
    protected final FieldDataSource source;
    protected final boolean shortenNames;
    protected final N root;

    public FieldDataHolder(FieldDataSource source, boolean shortenNames) {
        this.source = source;
        this.shortenNames = shortenNames;

        this.root = createNodeFrom(ObjectPath.EMPTY, source.rootData());

        Map<PathStep, N> nodes = new LinkedHashMap<>();

        for (var entry : source.initialRootFields().entrySet()) {
            nodes.put(entry.getKey(), createNodeFrom(ObjectPath.EMPTY.then(entry.getKey()), entry.getValue()));
        }

        this.root.initChildren(nodes);
    }

    protected abstract N createNodeFrom(ObjectPath path, FieldData data);

    CompletableFuture<Map<PathStep, N>> requestFields(ObjectPath path, int from, int limit) {
        return source.requestFieldsOf(path, from, limit)
            .thenApply(children -> {
                Map<PathStep, N> nodes = new LinkedHashMap<>();

                for (var entry : children.entrySet()) {
                    nodes.put(entry.getKey(), createNodeFrom(path.then(entry.getKey()), entry.getValue()));
                }

                return nodes;
            });
    }
}
