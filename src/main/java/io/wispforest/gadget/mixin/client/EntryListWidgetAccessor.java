package io.wispforest.gadget.mixin.client;

import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntryListWidget.class)
public interface EntryListWidgetAccessor {
    @Invoker
    int callGetRowTop(int index);

    @Accessor
    int getItemHeight();

    @Accessor
    int getLeft();

    @Accessor
    int getTop();

    @Accessor
    int getWidth();

    @Accessor
    int getHeight();
}
