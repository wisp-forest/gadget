package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.util.Observable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextBoxComponent.class)
public interface TextBoxComponentAccessor {
    @Accessor
    Observable<String> getTextValue();
}
