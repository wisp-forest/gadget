package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Eats events. Yum!
 * <p> Mainly used to make {@link io.wispforest.owo.ui.container.OverlayContainer} think that the click was inside the box
 * and not close.
 *
 * @param <C> Wrapped {@link Component} type.
 */
public class EventEaterWrapper<C extends Component> extends WrappingParentComponent<C> {
    public EventEaterWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
    }

    @Override
    public boolean onMouseDown(double mouseX, double mouseY, int button) {
        return super.onMouseDown(mouseX, mouseY, button) || isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public boolean onMouseUp(double mouseX, double mouseY, int button) {
        return super.onMouseDown(mouseX, mouseY, button) || isInBoundingBox(mouseX, mouseY);
    }

    @Override
    public void draw(OwoUIDrawContext ctx, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(ctx, mouseX, mouseY, partialTicks, delta);

        this.drawChildren(ctx, mouseX, mouseY, partialTicks, delta, children());
    }
}
