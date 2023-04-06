package io.wispforest.gadget.client.field;

import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.gadget.field.FieldDataNode;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.owo.ui.container.FlowLayout;

public class ClientFieldDataNode extends FieldDataNode<ClientFieldDataNode> {
    public FlowLayout containerComponent;
    public SubObjectContainer subObjectContainer;

    public ClientFieldDataNode(FieldDataIsland island, ObjectPath path, FieldData data) {
        super(island, path, data);
    }
}
