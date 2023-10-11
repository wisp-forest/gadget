package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.Gadget;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.EntryListWidget;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin<E extends EntryListWidget.Entry<E>> {
    @Shadow public abstract void setFocused(net.minecraft.client.gui.Element focused);

    @Shadow @Nullable protected abstract EntryListWidget.Entry<?> getEntryAtPosition(double x, double y);

    @Shadow @Nullable public abstract E getFocused();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void onRightClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            if (!Gadget.CONFIG.rightClickDump()) {
                cir.setReturnValue(false);
                return;
            }

            EntryListWidget.Entry<?> clickedEntry = this.getEntryAtPosition(mouseX, mouseY);
            if (clickedEntry != null) {
                if (clickedEntry.mouseClicked(mouseX, mouseY, button)) {
                    E parentEntry = this.getFocused();

                    if (clickedEntry != parentEntry && parentEntry instanceof ParentElement) {
                        ParentElement parentElement = (ParentElement) parentEntry;
                        parentElement.setFocused((Element) null);
                    }

                    this.setFocused(clickedEntry);
                    cir.setReturnValue(true);
                    return;
                }
            }

            cir.setReturnValue(false);
            return;
        }
    }
}
