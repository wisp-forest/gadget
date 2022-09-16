package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.pond.ContextMenuScreenAccess;
import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {SelectWorldScreen.class, MultiplayerScreen.class})
public class ContextMenuScreenMixin extends Screen implements ContextMenuScreenAccess {
    private DropdownComponent gadget$dropdown;
    private OwoUIAdapter<VerticalFlowLayout> gadget$adapter;

    protected ContextMenuScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init()V", at = @At("HEAD"))
    private void initAdapter(CallbackInfo ci) {
        if (gadget$adapter != null) {
            this.gadget$adapter.moveAndResize(0, 0, this.width, this.height);
            this.addDrawableChild(gadget$adapter);
            return;
        }

        gadget$adapter = OwoUIAdapter.create(this, Containers::verticalFlow);
        gadget$adapter.inflateAndMount();
    }

    @Override
    public void gadget$addDropdown(DropdownComponent dropdown) {
        gadget$dropdown = dropdown;
        gadget$adapter.rootComponent.child(dropdown);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (gadget$dropdown == null) return super.mouseClicked(mouseX, mouseY, button);

        if (!gadget$dropdown.isInBoundingBox(mouseX, mouseY)) {
            gadget$dropdown = null;
            gadget$adapter.rootComponent.clearChildren();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
