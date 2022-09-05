package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.core.Component;
import me.basiqueevangelist.gadget.desc.FieldObject;
import me.basiqueevangelist.gadget.network.FieldData;

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
