package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.gadget.mixin.NbtTypesAccessor;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.nbt.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class NbtDataIsland extends VerticalFlowLayout {
    private final Map<NbtPath, WidgetData> elements = new HashMap<>();

    final NbtCompound data;
    final Consumer<NbtCompound> reloader;

    public NbtDataIsland(NbtCompound data, Consumer<NbtCompound> reloader) {
        super(Sizing.content(), Sizing.content());

        this.data = data;
        this.reloader = reloader;

        for (String key : data.getKeys()) {
            makeComponent(new NbtPath(new String[] {key}), data.get(key));
        }
    }

    private void makeComponent(NbtPath path, NbtElement element) {
        VerticalFlowLayout full = Containers.verticalFlow(Sizing.content(), Sizing.content());
        HorizontalFlowLayout row = Containers.horizontalFlow(Sizing.content(), Sizing.content());

        full.child(row);

        var parentContainer = subContainerOf(path.parent());
        WidgetData widgetData = new WidgetData();
        widgetData.fullContainer = full;

        WidgetData old = elements.get(path);
        int idx = parentContainer.children().size();
        if (old != null) {
            idx = parentContainer.children().indexOf(old.fullContainer);

            parentContainer.removeChild(old.fullContainer);
            elements.entrySet().removeIf(entry -> entry.getKey().startsWith(path));
        }

        elements.put(path, widgetData);

        MutableText rowText = Text.literal("");
        LabelComponent label = Components.label(rowText);

        row.child(label);

        rowText.append(typeText(element.getNbtType(), ""));
        rowText.append(" " + path.name());

        if (element instanceof NbtString string) {
            rowText.append(Text.literal(" = ")
                .formatted(Formatting.GRAY));

            if (reloader != null) {
                row.child(new PrimitiveEditorWidget(this, path, string.asString(), NbtString::of));
            } else {
                rowText.append(Text.literal( string.asString() + " ")
                    .formatted(Formatting.GRAY));
            }
        } else if (element instanceof AbstractNbtNumber number) {
            rowText.append(Text.literal(" = ")
                .formatted(Formatting.GRAY));

            if (reloader != null) {
                row.child(new PrimitiveEditorWidget(this, path, number.numberValue(), text -> {
                    if (number instanceof NbtByte)
                        return NbtByte.of(Byte.parseByte(text));
                    else if (number instanceof NbtShort)
                        return NbtShort.of(Short.parseShort(text));
                    else if (number instanceof NbtInt)
                        return NbtInt.of(Integer.parseInt(text));
                    else if (number instanceof NbtLong)
                        return NbtLong.of(Long.parseLong(text));
                    else if (number instanceof NbtFloat)
                        return NbtFloat.of(Float.parseFloat(text));
                    else if (number instanceof NbtDouble)
                        return NbtDouble.of(Double.parseDouble(text));
                    else
                        throw new IllegalStateException("Unknown AbstractNbtNumber type!");
                }));
            } else {
                rowText.append(Text.literal( number.numberValue() + " ")
                    .formatted(Formatting.GRAY));
            }
        } else if (element instanceof NbtCompound compound) {
            rowText.append(" ");

            widgetData.subContainer = new SubObjectContainer(() -> {}, () -> {});

            row.child(widgetData.subContainer.getSpinnyBoi());

            full.child(widgetData.subContainer);

            for (String key : compound.getKeys()) {
                NbtElement sub = compound.get(key);
                var subPath = path.then(key);

                makeComponent(subPath, sub);
            }

            var plusLabel = Components.label(Text.of("+ "));

            GuiUtil.hoverBlue(plusLabel);
            plusLabel.cursorStyle(CursorStyle.HAND);
            typeSelector(plusLabel, type -> {

            });

            row.child(plusLabel);
        } else if (element instanceof AbstractNbtList<?> list) {
            rowText.append(" ");

            widgetData.subContainer = new SubObjectContainer(() -> {}, () -> {});

            row.child(widgetData.subContainer.getSpinnyBoi());

            full.child(widgetData.subContainer);

            for (int i = 0; i < list.size(); i++) {
                NbtElement sub = list.get(i);
                var subPath = path.then(String.valueOf(i));

                makeComponent(subPath, sub);
            }
        }

        VerticalFlowLayout target = subContainerOf(path.parent());

        if (reloader != null) {
            var crossLabel = Components.label(Text.literal("❌"));
            crossLabel.cursorStyle(CursorStyle.HAND);
            GuiUtil.hoverBlue(crossLabel);
            crossLabel.mouseDown().subscribe((mouseX, mouseY, button) -> {
                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                path.remove(data);
                reloader.accept(data);
                target.removeChild(full);
                elements.entrySet().removeIf(entry -> entry.getKey().startsWith(path));

                return true;
            });
            row.child(crossLabel);
        }

        row
            .margins(Insets.both(0, 2))
            .allowOverflow(true);

        target.child(idx, full);
    }

    private void typeSelector(Component attachTo, Consumer<NbtType<?>> consumer) {
        attachTo.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            var dropdown = Components.dropdown(Sizing.content());
            var root = (FlowLayout) GuiUtil.root(this);

            dropdown
                .positioning(Positioning.absolute(
                    (int) (attachTo.y() + mouseY),
                    (int) (attachTo.y() + mouseY)
                ));

            ((ParentComponent) dropdown.children().get(0)).padding(Insets.of(3));

            dropdown.focusLost().subscribe(() -> {
                root.removeChild(dropdown);
            });

            for (NbtType<?> type : NbtTypesAccessor.getVALUES()) {
                dropdown.button(typeText(type, ".full"), unused -> {
                });
            }

            root.child(dropdown);
            root.focusHandler().focus(dropdown, FocusSource.MOUSE_CLICK);

            return true;
        });
    }

    private MutableText typeText(NbtType<?> type, String suffix) {
        String name = "";

        if (type == NbtCompound.TYPE)
            name = "compound";
        else if (type == NbtList.TYPE)
            name = "list";
        else if (type == NbtByteArray.TYPE)
            name = "byte_array";
        else if (type == NbtIntArray.TYPE)
            name = "int_array";
        else if (type == NbtLongArray.TYPE)
            name = "long_array";
        else if (type == NbtString.TYPE)
            name = "string";
        else if (type == NbtByte.TYPE)
            name = "byte";
        else if (type == NbtShort.TYPE)
            name = "short";
        else if (type == NbtInt.TYPE)
            name = "int";
        else if (type == NbtLong.TYPE)
            name = "long";
        else if (type == NbtFloat.TYPE)
            name = "float";
        else if (type == NbtDouble.TYPE)
            name = "double";
        else if (type == NbtEnd.TYPE)
            name = "end";
        else
            name = "unknown";

        return Text.translatable("text.gadget.nbt." + name + suffix);
    }

    public void reloadPath(NbtPath path) {
        if (path.steps().length == 0) {
            clearChildren();

            for (String key : data.getKeys()) {
                makeComponent(new NbtPath(new String[] {key}), data.get(key));
            }
        } else {
            makeComponent(path, path.follow(data));
        }
    }

    private VerticalFlowLayout subContainerOf(NbtPath path) {
        if (path.steps().length == 0)
            return this;
        else
            return elements.get(path).subContainer;
    }

    private static class WidgetData {
        private VerticalFlowLayout fullContainer;
        private SubObjectContainer subContainer;

    }

}
