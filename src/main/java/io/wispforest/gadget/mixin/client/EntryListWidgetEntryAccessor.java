package io.wispforest.gadget.mixin.client;

import net.minecraft.client.gui.widget.EntryListWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntryListWidget.Entry.class)
public interface EntryListWidgetEntryAccessor {
    @Accessor
    EntryListWidget<?> getParentList();
}
