package io.wispforest.gadget.client.field;

import io.wispforest.gadget.desc.PrimitiveFieldObject;
import io.wispforest.gadget.desc.edit.PrimitiveEditData;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.path.ObjectPath;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class PrimitiveFieldWidget extends FlowLayout {
    private final PrimitiveEditData editData;
    private final FieldDataIsland island;
    private final ObjectPath fieldPath;

    private final LabelComponent contentsLabel;
    private final LabelComponent editLabel;
    private final TextFieldWidget editField;

    protected PrimitiveFieldWidget(FieldDataIsland island, ObjectPath fieldPath, PrimitiveFieldObject pfo) {
        super(Sizing.content(), Sizing.content(), Algorithm.HORIZONTAL);
        this.island = island;
        this.fieldPath = fieldPath;

        this.contentsLabel = Components.label(
            Text.literal(pfo.contents())
                .formatted(Formatting.GRAY)
        );
        this.editLabel = Components.label(Text.literal(" ✎ "));
        this.editField = Components.textBox(Sizing.fixed(100));
        this.editData = pfo.editData().orElseThrow();

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

            island.primitiveSetter.accept(fieldPath, new PrimitiveEditData(editData.type(), editField.getText()));

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
        editField.setText(editData.data());
        editField.setCursorToStart();

        if (focusHandler() != null)
            focusHandler().focus(editField, FocusSource.MOUSE_CLICK);

        editField.setFocused(true);
    }
}
