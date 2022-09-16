package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

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
}
