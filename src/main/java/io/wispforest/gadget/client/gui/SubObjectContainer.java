package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Copied from owo-ui's CollapsibleContainer
public class SubObjectContainer extends FlowLayout {

    public static final Surface SURFACE = (matrices, component) -> Drawer.fill(matrices,
        component.x() + 5,
        component.y(),
        component.x() + 6,
        component.y() + component.height(),
        0x77FFFFFF
    );
    private final Consumer<SubObjectContainer> loader;
    private final Consumer<SubObjectContainer> unloader;

    protected final List<Component> collapsibleChildren = new ArrayList<>();
    protected boolean expanded;

    protected final SpinnyBoiComponent spinnyBoi;

    public SubObjectContainer(Consumer<SubObjectContainer> loader, Consumer<SubObjectContainer> unloader) {
        super(Sizing.content(), Sizing.content(), Algorithm.VERTICAL);
        this.loader = loader;
        this.unloader = unloader;
        this.surface(SURFACE);
        this.padding(Insets.left(15));

        this.allowOverflow(true);

        this.spinnyBoi = new SpinnyBoiComponent();
        this.spinnyBoi
                .cursorStyle(CursorStyle.HAND);

        this.expanded = false;
        this.spinnyBoi.targetRotation = 0;
        this.spinnyBoi.rotation = this.spinnyBoi.targetRotation;

        GuiUtil.hoverBlue(this.spinnyBoi);

        this.spinnyBoi.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.toggleExpansion();
            UISounds.playInteractionSound();

            return true;
        });
    }

    public Component getSpinnyBoi() {
        return spinnyBoi;
    }

    protected void toggleExpansion() {
        if (expanded) {
            this.children.removeAll(collapsibleChildren);
            this.spinnyBoi.targetRotation = 0;
        } else {
            this.children.addAll(this.collapsibleChildren);
            this.spinnyBoi.targetRotation = 90;
        }
        this.updateLayout();

        this.expanded = !this.expanded;

        if (expanded) {
            loader.accept(this);
        } else {
            unloader.accept(this);
        }
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.KEYBOARD_CYCLE;
    }

    @Override
    public boolean onKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.toggleExpansion();

            super.onKeyPress(keyCode, scanCode, modifiers);
            return true;
        }

        return super.onKeyPress(keyCode, scanCode, modifiers);
    }

    @Override
    public FlowLayout child(Component child) {
        this.collapsibleChildren.add(child);

        if (this.expanded) {
            super.child(child);
        }
        return this;
    }

    @Override
    public FlowLayout child(int index, Component child) {
        this.collapsibleChildren.add(index, child);

        if (this.expanded) {
            super.child(index, child);
        }
        return this;
    }

    @Override
    public FlowLayout removeChild(Component child) {
        this.collapsibleChildren.remove(child);
        return super.removeChild(child);
    }

    @Override
    public FlowLayout clearChildren() {
        this.collapsibleChildren.clear();
        return super.clearChildren();
    }

    public List<Component> collapsibleChildren() {
        return collapsibleChildren;
    }

    protected static class SpinnyBoiComponent extends LabelComponent {

        protected float rotation = 90;
        protected float targetRotation = 90;

        public SpinnyBoiComponent() {
            super(Text.literal(">"));
            this.margins(Insets.horizontal(4));
        }

        @Override
        public void update(float delta, int mouseX, int mouseY) {
            super.update(delta, mouseX, mouseY);
            this.rotation += (this.targetRotation - this.rotation) * delta * .65;
        }

        @Override
        public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
            matrices.push();
            matrices.translate(this.x + this.width / 2f - 1, this.y + this.height / 2f - 1, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotation));
            matrices.translate(-(this.x + this.width / 2f - 1), -(this.y + this.height / 2f - 1), 0);

            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
            matrices.pop();
        }
    }
}

