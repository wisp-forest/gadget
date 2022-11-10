package io.wispforest.gadget.client.gui;

import io.netty.buffer.ByteBuf;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.mixin.client.EntryListWidgetAccessor;
import io.wispforest.gadget.mixin.client.EntryListWidgetEntryAccessor;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public final class GuiUtil {
    private GuiUtil() {

    }

    public static void hoverBlue(LabelComponent label) {
        label.mouseEnter().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.BLUE)));

        label.mouseLeave().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.WHITE)));
    }

    public static void semiButton(LabelComponent label, Runnable onPressed) {
        hoverBlue(label);
        label.cursorStyle(CursorStyle.HAND);

        label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playButtonSound();
            onPressed.run();

            return true;
        });
    }

    public static void semiButton(LabelComponent label, BiConsumer<Double, Double> onPressed) {
        hoverBlue(label);
        label.cursorStyle(CursorStyle.HAND);

        label.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playButtonSound();
            onPressed.accept(mouseX, mouseY);

            return true;
        });
    }

    public static ParentComponent root(Component component) {
        ParentComponent root = component.parent();

        if (root == null)
            throw new IllegalStateException();

        while (root.hasParent())
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
        if (element instanceof ClickableWidget widget) {
            return widget.x;
        } else if (element instanceof EntryListWidget<?> list) {
            return ((EntryListWidgetAccessor) list).getLeft();
        } else if (element instanceof EntryListWidget.Entry<?> entry) {
            var list = ((EntryListWidgetEntryAccessor) entry).getParentList();

            return list.getRowLeft();
        } else {
            return -1;
        }
    }

    public static int y(Element element) {
        if (element instanceof ClickableWidget widget) {
            return widget.y;
        } else if (element instanceof EntryListWidget<?> list) {
            return ((EntryListWidgetAccessor) list).getTop();
        } else if (element instanceof EntryListWidget.Entry<?> entry) {
            var list = ((EntryListWidgetEntryAccessor) entry).getParentList();

            return ((EntryListWidgetAccessor) list).callGetRowTop(list.children().indexOf(entry));
        } else {
            return -1;
        }
    }

    public static int width(Element element) {
        if (element instanceof ClickableWidget widget) {
            return widget.getWidth();
        } else if (element instanceof EntryListWidget<?> list) {
            return ((EntryListWidgetAccessor) list).getWidth();
        } else if (element instanceof EntryListWidget.Entry<?> entry) {
            var list = ((EntryListWidgetEntryAccessor) entry).getParentList();

            return list.getRowWidth();
        } else {
            return -1;
        }
    }

    public static int height(Element element) {
        if (element instanceof ClickableWidget widget) {
            return widget.getHeight();
        } else if (element instanceof EntryListWidget<?> list) {
            return ((EntryListWidgetAccessor) list).getHeight();
        } else if (element instanceof EntryListWidget.Entry<?> entry) {
            var list = ((EntryListWidgetEntryAccessor) entry).getParentList();

            return ((EntryListWidgetAccessor) list).getItemHeight();
        } else {
            return -1;
        }
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

    private static final int INVALID_COLOR = 0xEB1D36;
    private static final int VALID_COLOR = 0x28FFBF;

    public static void textFieldVerifier(TextFieldWidget textField, Predicate<String> verifier) {
        textField.setChangedListener(
            text -> textField.setEditableColor(verifier.test(text) ? VALID_COLOR : INVALID_COLOR));
    }

    public static VerticalFlowLayout hexDump(ByteBuf buf) {
        VerticalFlowLayout view = Containers.verticalFlow(Sizing.content(), Sizing.content());

        List<Component> expandedChildren = new ArrayList<>();

        int origReaderIdx = buf.readerIndex();

        short[] bytes = new short[16];

        int index = 0;
        while (buf.readableBytes() > 0) {
            StringBuilder line = new StringBuilder();

            line.append(String.format("%04x  ", index));

            int i;
            for (i = 0; i < 16 && buf.readableBytes() > 0; i++) {
                short b = buf.readUnsignedByte();
                bytes[i] = (byte) (b & 0xff);

                line.append(String.format("%02x ", b));
                index++;
            }

            line.append("   ".repeat(Math.max(0, 16 - i)));

            for (int j = 0; j < i; j++) {
                if (bytes[j] >= 32 && bytes[j] < 127)
                    line.append((char) bytes[j]);
                else
                    line.append('.');
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

            semiButton(ellipsis, () -> {
                view.removeChild(ellipsis);
                view.children(expandedChildren);
            });

            view.child(ellipsis);
        }

        return view;
    }
}
