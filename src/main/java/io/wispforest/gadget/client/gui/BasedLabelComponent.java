package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.Size;
import net.minecraft.text.Text;

public class BasedLabelComponent extends LabelComponent {
    public BasedLabelComponent(Text text) {
        super(text);
    }

    @Override
    public void inflate(Size space) {
        int newMaxWidth = space.width();

        if (maxWidth != newMaxWidth)
            maxWidth = newMaxWidth;

        super.inflate(space);
    }
}
