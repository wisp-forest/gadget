package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.container.WrappingParentComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.util.math.MatrixStack;

public class LayoutCacheWrapper<C extends Component> extends WrappingParentComponent<C> {
    private Size prevSpace;

    public LayoutCacheWrapper(C child) {
        super(Sizing.content(), Sizing.content(), child);
    }

    @Override
    public void layout(Size space) {
        if (!space.equals(prevSpace)) {
            prevSpace = space;
            this.child.inflate(this.calculateChildSpace(space));
        }

        this.child.mount(this, this.childMountX(), this.childMountY());
    }

    @Override
    public void onChildMutated(Component child) {
        this.prevSpace = null;
        super.onChildMutated(child);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(matrices, mouseX, mouseY, partialTicks, delta);

        this.drawChildren(matrices, mouseX, mouseY, partialTicks, delta, children());
    }
}
