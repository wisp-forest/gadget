package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.function.Function;

public class PrimitiveEditorWidget extends FlowLayout {
    private final NbtDataIsland island;
    private final NbtPath path;
    private final Object value;
    private final Function<String, NbtElement> parser;

    private final LabelComponent contentsLabel;
    private final LabelComponent editLabel;
    private final TextFieldWidget editField;

    protected PrimitiveEditorWidget(NbtDataIsland island, NbtPath path, Object value, Function<String, NbtElement> parser) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
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

        GuiUtil.semiButton(this.editLabel, this::startEditing);
        this.editField.focusLost().subscribe(this::editFieldFocusLost);
        this.editField.keyPress().subscribe(this::editFieldKeyPressed);
        this.editField
            .verticalSizing(Sizing.fixed(8));

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

    private void startEditing() {
        removeChild(contentsLabel);
        removeChild(editLabel);

        child(editField);
        editField.setText(value.toString());
        editField.setCursorToStart(false);

        if (focusHandler() != null)
            focusHandler().focus(editField, FocusSource.MOUSE_CLICK);

        editField.setFocused(true);
    }
}
