package io.wispforest.gadget.field;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.desc.ComplexFieldObject;
import io.wispforest.gadget.desc.ErrorFieldObject;
import io.wispforest.gadget.desc.PrimitiveFieldObject;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.FieldPathStep;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import io.wispforest.gadget.util.FormattedDumper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class FieldDataHolder<N extends FieldDataNode<N>> {
    protected final FieldDataSource source;
    protected final boolean shortenNames;
    public final N root;

    public FieldDataHolder(FieldDataSource source, boolean shortenNames) {
        this.source = source;
        this.shortenNames = shortenNames;

        this.root = createNodeFrom(ObjectPath.EMPTY, source.rootData());
        this.root.initChildren(processMap(ObjectPath.EMPTY, source.initialRootFields()));
    }

    public N get(ObjectPath path) {
        N current = root;

        for (PathStep step : path.steps()) {
            var children = current.childrenOrNull();

            if (children == null) return null;

            current = children.get(step);

            if (current == null) return null;
        }

        return current;
    }

    public CompletableFuture<Void> dumpToText(FormattedDumper dumper, int indent, N startingAt, int depthLeft) {
        return startingAt
            .ensureChildren()
            .thenCompose(children -> {
                CompletableFuture<Void> runningTotal = CompletableFuture.completedFuture(null);

                for (var entry : children.entrySet()) {
                    runningTotal = runningTotal.thenCompose(ignored -> {
                        StringBuilder head = new StringBuilder();
                        var data = entry.getValue();

                        head.append(data.fieldObj().type().charAt(0));
                        head.append(" ");
                        head.append(entry.getKey().toString());

                        if (data.isMixin())
                            head.append(" (mixin)");

                        if (data.fieldObj() instanceof PrimitiveFieldObject pfo) {
                            head.append(" = ").append(pfo.contents());
                        } else if (data.fieldObj() instanceof ErrorFieldObject) {
                            head.append(" error!");
                        } else if (data.fieldObj() instanceof ComplexFieldObject cfo) {
                            String text = cfo.text();

                            if (shortenNames)
                                text = text.substring(text.lastIndexOf('.') + 1);

                            head.append(" ").append(text);
                        }

                        dumper.write(indent, head.toString());

                        if (data.fieldObj() instanceof ErrorFieldObject efo) {
                            dumper.writeLines(indent + 1, efo.fullExceptionText());
                        }

                        if (data.fieldObj() instanceof ComplexFieldObject && depthLeft > 0) {
                            return dumpToText(dumper, indent + 1, data, depthLeft - 1);
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    });
                }

                return runningTotal;
            });
    }

    protected abstract N createNodeFrom(ObjectPath path, FieldData data);

    Map<PathStep, N> processMap(ObjectPath base, Map<PathStep, FieldData> originalChildren) {
        Map<PathStep, N> nodes = new LinkedHashMap<>();

        for (var entry : originalChildren.entrySet()) {
            if (entry.getKey() instanceof FieldPathStep field
             && Gadget.CONFIG.hiddenFields().contains(field.fieldId()))
                continue;

            nodes.put(entry.getKey(), createNodeFrom(base.then(entry.getKey()), entry.getValue()));
        }

        return nodes;
    }

    CompletableFuture<Map<PathStep, N>> requestFields(ObjectPath path, int from, int limit) {
        return source.requestFieldsOf(path, from, limit)
            .thenApply(children -> processMap(path, children));
    }
}
