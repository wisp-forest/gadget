package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class PrimitiveEditorWidget extends HorizontalFlowLayout {
    private final NbtDataIsland island;
    private final NbtPath path;
    private final Object value;
    private final Function<String, NbtElement> parser;

    private final LabelComponent contentsLabel;
    private final LabelComponent editLabel;
    private final TextFieldWidget editField;

    protected PrimitiveEditorWidget(NbtDataIsland island, NbtPath path, Object value, Function<String, NbtElement> parser) {
        super(Sizing.content(), Sizing.content());
        this.island = island;
        this.path = path;

        this.contentsLabel = Components.label(
            Text.literal(value.toString())
                .formatted(Formatting.GRAY)
        );
        this.value = value;
        this.parser = parser;
        this.editLabel = Components.label(Text.literal(" ✎ "));
        this.editField = Components.textBox(Sizing.fixed(100));

        this.editLabel.mouseDown().subscribe(this::editLabelMouseDown);
        this.editField.focusLost().subscribe(this::editFieldFocusLost);
        this.editField.keyPress().subscribe(this::editFieldKeyPressed);
        this.editField
            .verticalSizing(Sizing.fixed(8));
        this.editLabel
            .cursorStyle(CursorStyle.HAND);

        GuiUtil.hoverBlue(this.editLabel);

        child(contentsLabel);
        child(editLabel);
    }

    private boolean editFieldKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            UISounds.playButtonSound();

            path.set(island.data, parser.apply(editField.getText()));

            island.reloadPath(path);
            island.reloader.accept(island.data);

            removeChild(editField);

            child(contentsLabel);
            child(editLabel);
            return true;
        }

        return false;
    }

    private void editFieldFocusLost() {
        removeChild(editField);

        child(contentsLabel);
        child(editLabel);
    }

    private boolean editLabelMouseDown(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        UISounds.playInteractionSound();

        removeChild(contentsLabel);
        removeChild(editLabel);

        child(editField);
        editField.setText(value.toString());
        editField.setCursorToStart();

        if (focusHandler() != null)
            focusHandler().focus(editField, FocusSource.MOUSE_CLICK);

        editField.setTextFieldFocused(true);

        return true;
    }
}
