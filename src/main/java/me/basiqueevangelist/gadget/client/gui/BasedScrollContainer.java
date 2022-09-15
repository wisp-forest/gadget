package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class BasedScrollContainer<C extends Component> extends ScrollContainer<C> {
    public BasedScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(direction, horizontalSizing, verticalSizing, child);
    }

    @Override
    public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {

    }
}
