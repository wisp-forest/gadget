package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import net.minecraft.text.Text;

public class BasedLabelComponent extends LabelComponent {
    public BasedLabelComponent(Text text) {
        super(text);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);

        int newMaxWidth = (int) (GuiUtil.root(this).width() * 0.85);

        if (maxWidth() != newMaxWidth)
            maxWidth(newMaxWidth);
    }
}
