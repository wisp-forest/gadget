package io.wispforest.gadget.client.field;

import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.gadget.desc.FieldObject;
import io.wispforest.gadget.network.FieldData;
import io.wispforest.owo.ui.core.Component;

public class ClientFieldData {
    public FieldObject obj;
    public Component containerComponent;
    public SubObjectContainer subObjectContainer;
    public boolean isMixin;
    public boolean isFinal;

    public ClientFieldData(FieldData data) {
        this.obj = data.obj();
        this.isMixin = data.isMixin();
        this.isFinal = data.isFinal();
    }
}
