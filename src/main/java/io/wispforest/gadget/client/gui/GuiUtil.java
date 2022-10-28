package io.wispforest.gadget.client.gui;

import io.netty.buffer.ByteBuf;
import io.wispforest.gadget.Gadget;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class GuiUtil {
    private GuiUtil() {

    }

    public static void hoverBlue(LabelComponent label) {
        label.mouseEnter().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.BLUE)));

        label.mouseLeave().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.WHITE)));
    }

    public static ParentComponent root(Component component) {
        ParentComponent root = component.parent();

        if (root == null)
            throw new IllegalStateException();

        if (root.hasParent())
            root = root.parent();

        return root;
    }

    public static void collectChildren(ParentElement root, List<Element> children) {
        for (var child : root.children()) {
            children.add(child);

            if (child instanceof ParentElement parent)
                collectChildren(parent, children);
        }
    }

    public static Element childAt(ParentElement parent, int x, int y) {
        var iter = parent.children().listIterator(parent.children().size());

        while (iter.hasPrevious()) {
            var child = iter.previous();
            if (inBoundingBox(child, x, y)) {
                if (child instanceof ParentElement other) {
                    return childAt(other, x, y);
                } else {
                    return child;
                }
            }
        }

        return inBoundingBox(parent, x, y) ? parent : null;
    }

    public static int x(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.x;
        else
            return -1;
    }

    public static int y(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.y;
        else
            return -1;
    }

    public static int width(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.getWidth();
        else
            return -1;
    }

    public static int height(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.getHeight();
        else
            return -1;
    }

    public static boolean isVisible(Element element) {
        if (element instanceof ClickableWidget widget)
            return widget.visible;
        else
            return true;
    }

    public static boolean inBoundingBox(Element e, int x, int y) {
        if (x(e) == -1) return false;

        return x >= x(e)
            && y >= y(e)
            && x < (x(e) + width(e))
            && y < (y(e) + height(e));
    }

    public static VerticalFlowLayout hexDump(ByteBuf buf) {
        VerticalFlowLayout view = Containers.verticalFlow(Sizing.content(), Sizing.content());

        List<Component> expandedChildren = new ArrayList<>();

        int origReaderIdx = buf.readerIndex();

        int index = 0;
        while (buf.readableBytes() > 0) {
            StringBuilder line = new StringBuilder();

            line.append(String.format("%04x  ", index));

            for (int i = 0; i < 16 && buf.readableBytes() > 0; i++) {
                int b = buf.readUnsignedByte();

                line.append(String.format("%02x ", b));
                index++;
            }

            var label = Components.label(Text.literal(line.toString())
                    .styled(x -> x.withFont(Gadget.id("monocraft"))))
                .margins(Insets.bottom(3));

            if (view.children().size() > 10)
                expandedChildren.add(label);
            else
                view.child(label);
        }

        buf.readerIndex(origReaderIdx);

        if (expandedChildren.size() > 0) {
            LabelComponent ellipsis = Components.label(Text.literal("..."));

            hoverBlue(ellipsis);
            ellipsis.cursorStyle(CursorStyle.HAND);
            ellipsis.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                view.removeChild(ellipsis);
                view.children(expandedChildren);

                return true;
            });

            view.child(ellipsis);
        }

        return view;
    }
}
