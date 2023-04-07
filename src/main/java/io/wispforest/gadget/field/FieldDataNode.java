package io.wispforest.gadget.field;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.desc.FieldObject;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.FieldPathStep;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.gadget.path.PathStep;
import io.wispforest.gadget.util.WeakObservableDispatcher;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FieldDataNode<N extends FieldDataNode<N>> {
    private static final WeakObservableDispatcher<List<String>> HIDDEN_FIELDS = new WeakObservableDispatcher<>();

    static {
        Gadget.CONFIG.subscribeToHiddenFields(HIDDEN_FIELDS::handle);
    }

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

        HIDDEN_FIELDS.register(newList -> {
            if (path.steps().length > 0
             && path.last() instanceof FieldPathStep field
             && newList.contains(field.fieldId())) {
                onRemoved();

                return true;
            }

            return false;
        });
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

    public void onRemoved() {
        if (children != null) {
            resetChildren();
        }
    }

    public void resetChildren() {
        if (children != null) {
            for (var entry : children.entrySet()) {
                entry.getValue().onRemoved();
            }
        }

        children = null;
    }

    void initChildren(Map<PathStep, N> newChildren) {
        this.children = newChildren;
    }
}
