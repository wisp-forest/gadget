package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class CringeComponent extends BaseComponent {
    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return 0;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return 0;
    }

    @Override
    public void draw(OwoUIDrawContext ctx, int mouseX, int mouseY, float partialTicks, float delta) {

    }
}
