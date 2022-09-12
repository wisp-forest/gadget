package me.basiqueevangelist.gadget.client.field;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import me.basiqueevangelist.gadget.desc.PrimitiveFieldObject;
import me.basiqueevangelist.gadget.desc.edit.PrimitiveEditData;
import me.basiqueevangelist.gadget.path.ObjectPath;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public class PrimitiveFieldWidget extends HorizontalFlowLayout {
    private final PrimitiveEditData editData;
    private final FieldDataIsland island;
    private final ObjectPath fieldPath;

    private final LabelComponent contentsLabel;
    private final LabelComponent editLabel;
    private final TextFieldWidget editField;

    protected PrimitiveFieldWidget(FieldDataIsland island, ObjectPath fieldPath, boolean isFinal, PrimitiveFieldObject pfo) {
        super(Sizing.content(), Sizing.content());
        this.island = island;
        this.fieldPath = fieldPath;

        this.contentsLabel = Components.label(
            Text.literal(pfo.contents())
                .formatted(Formatting.GRAY)
        );
        this.editLabel = Components.label(Text.literal(" ✎ "));
        this.editField = Components.textBox(Sizing.fixed(100));
        this.editData = isFinal ? null : pfo.editData().orElse(null);

        this.editLabel.mouseDown().subscribe(this::editLabelMouseDown);
        this.editField.keyPress().subscribe(this::editFieldKeyPressed);
        this.editField
            .verticalSizing(Sizing.fixed(8));

        child(Components.label(
            Text.literal(" = ")
                .formatted(Formatting.GRAY)
        ));
        child(contentsLabel);

        if (editData != null && island.primitiveSetter != null)
            child(editLabel);
    }

    private boolean editFieldKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            removeChild(editField);

            child(contentsLabel);
            child(editLabel);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
            UISounds.playButtonSound();

            island.primitiveSetter.accept(fieldPath, new PrimitiveEditData(editData.type(), editField.getText()));

            removeChild(editField);

            child(contentsLabel);
            child(editLabel);
            return true;
        }

        return false;
    }

    private boolean editLabelMouseDown(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

        UISounds.playInteractionSound();

        removeChild(contentsLabel);
        removeChild(editLabel);

        child(editField);
        editField.setText(editData.data());
        editField.setCursorToStart();

        return true;
    }
}
