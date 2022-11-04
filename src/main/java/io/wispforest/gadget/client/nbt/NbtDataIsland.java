package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.SubObjectContainer;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
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

    private final NbtCompound data;
    private final Consumer<NbtCompound> reloader;

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

        WidgetData widgetData = new WidgetData();
        widgetData.fullContainer = full;

        elements.put(path, widgetData);

        MutableText rowText = Text.literal("");
        LabelComponent label = Components.label(rowText);

        row.child(label);

        rowText.append(typeText(element));
        rowText.append(" " + path.name());

        if (element instanceof NbtString string) {
            rowText.append(Text.literal(" = " + string.asString() + " ")
                .formatted(Formatting.GRAY));
        } else if (element instanceof AbstractNbtNumber number) {
            rowText.append(Text.literal(" = " + number.numberValue() + " ")
                .formatted(Formatting.GRAY));
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

        var upPath = path.parent();
        VerticalFlowLayout target;

        if (upPath.steps().length == 0)
            target = this;
        else
            target = elements.get(upPath).subContainer;

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

        target.child(full);
    }

    private MutableText typeText(NbtElement element) {
        if (element instanceof NbtCompound)
            return TextOps.withColor("c", 0x0197F6);
        else if (element instanceof NbtList)
            return TextOps.withColor("L", 0x1560BD);
        else if (element instanceof NbtByteArray)
            return TextOps.withColor("ba", 0x3A7CA5);
        else if (element instanceof NbtIntArray)
            return TextOps.withColor("ia", 0x0047AB);
        else if (element instanceof NbtLongArray)
            return TextOps.withColor("la", 0x74B3CE);
        else if (element instanceof NbtString)
            return TextOps.withColor("s", 0xFFFF00);
        else if (element instanceof NbtByte)
            return TextOps.withColor("b", 0x03C04A);
        else if (element instanceof NbtShort)
            return TextOps.withColor("s", 0x597D35);
        else if (element instanceof NbtInt)
            return TextOps.withColor("i", 0x00FF00);
        else if (element instanceof NbtLong)
            return TextOps.withColor("l", 0x03AC13);
        else if (element instanceof NbtFloat)
            return TextOps.withColor("f", 0xED7014);
        else if (element instanceof NbtDouble)
            return TextOps.withColor("d", 0xEC9706);
        else if (element instanceof NbtEnd)
            return TextOps.withFormatting("e", Formatting.GRAY);
        else
            return TextOps.withFormatting("u", Formatting.DARK_GRAY);
    }

    private static class WidgetData {
        private VerticalFlowLayout fullContainer;
        private SubObjectContainer subContainer;

    }

}
