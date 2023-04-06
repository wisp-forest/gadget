package io.wispforest.gadget.field;

import io.wispforest.gadget.desc.FieldObject;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FieldDataNode<N extends FieldDataNode<N>> {
    private final FieldDataHolder<N> holder;
    private final ObjectPath path;
    private @Nullable Map<PathStep, N> children = null;
    private final FieldObject fieldObj;
    private final boolean isFinal;
    private final boolean isMixin;

    public FieldDataNode(FieldDataHolder<N> holder, ObjectPath path, FieldData data) {
        this.holder = holder;
        this.path = path;
        this.fieldObj = data.obj();
        this.isFinal = data.isFinal();
        this.isMixin = data.isMixin();
    }

    public ObjectPath path() {
        return path;
    }

    public FieldObject fieldObj() {
        return fieldObj;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public boolean isMixin() {
        return isMixin;
    }

    public @Nullable Map<PathStep, N> childrenOrNull() {
        return children;
    }

    public CompletableFuture<Map<PathStep, N>> ensureChildren() {
        if (children != null) return CompletableFuture.completedFuture(children);

        return holder.requestFields(path, 0, -1)
            .thenApply(children -> {
                this.children = children;
                return children;
            });
    }

    public void resetChildren() {
        children = null;
    }

    void initChildren(Map<PathStep, N> newChildren) {
        this.children = newChildren;
    }
}
