package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class SidebarBuilder {
    private final FlowLayout layout = Containers.verticalFlow(Sizing.content(), Sizing.content());

    {
        layout
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(2));
    }

    public FlowLayout layout() {
        return layout;
    }

    public void button(String translationKeyBase, OnPressHandler handler) {
        button(Text.translatable(translationKeyBase), Text.translatable(translationKeyBase + ".tooltip"), handler);
    }

    public void button(Text icon, @Nullable Text tooltip, OnPressHandler handler) {
        Button button = new Button(icon, tooltip);

        button.mouseDown().subscribe((mouseX, mouseY, mouseButton) -> {
            if (mouseButton != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            handler.onPress((int) mouseX, (int) mouseY);

            return true;
        });

        layout.child(button);
    }

    public static class Button extends FlowLayout {
        public Button(Text icon, Text tooltip) {
            super(Sizing.fixed(16), Sizing.fixed(16), Algorithm.VERTICAL);

            child(Components.label(icon)
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );
            cursorStyle(CursorStyle.HAND);

            if (tooltip != null)
                tooltip(tooltip);

            mouseEnter().subscribe(
                () -> surface(Surface.flat(0x80ffffff)));

            mouseLeave().subscribe(
                () -> surface(Surface.BLANK));
        }
    }

    public interface OnPressHandler {
        void onPress(int mouseX, int mouseY);
    }
}
