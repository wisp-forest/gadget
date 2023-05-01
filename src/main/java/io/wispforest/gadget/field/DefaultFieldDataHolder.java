package io.wispforest.gadget.field;

import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;

public class DefaultFieldDataHolder extends FieldDataHolder<DefaultFieldDataHolder.Node> {
    public DefaultFieldDataHolder(FieldDataSource source, boolean shortenNames) {
        super(source, shortenNames);
    }

    @Override
    protected Node createNodeFrom(ObjectPath path, FieldData data) {
        return new Node(this, path, data);
    }

    public static class Node extends FieldDataNode<Node> {
        public Node(FieldDataHolder<Node> holder, ObjectPath path, FieldData data) {
            super(holder, path, data);
        }
    }
}
