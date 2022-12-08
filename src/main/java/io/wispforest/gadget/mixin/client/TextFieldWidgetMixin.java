package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.mixin.owo.TextBoxComponentAccessor;
import io.wispforest.owo.ui.component.TextBoxComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {
    @SuppressWarnings("ConstantConditions")
    @Inject(method = "onChanged", at = @At("HEAD"))
    private void mald(String newText, CallbackInfo ci) {
        if ((Object) this instanceof TextBoxComponent textBox) {
            ((TextBoxComponentAccessor) textBox).getTextValue().set(newText);
        }
    }
}
