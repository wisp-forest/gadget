package io.wispforest.gadget.client.gui.inspector;

import io.wispforest.gadget.mixin.client.EntryListWidgetAccessor;
import io.wispforest.gadget.mixin.client.EntryListWidgetEntryAccessor;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.EntryListWidget;

public final class VanillaSupport {
    private VanillaSupport() {

    }

    public static void init() {
        ElementUtils.registerRootLister((screen, list) -> list.add(screen));

        ElementUtils.registerElementSupport(ClickableWidget.class, ElementSupport.fromLambda(
            ClickableWidget::getX,
            ClickableWidget::getY,
            ClickableWidget::getWidth,
            ClickableWidget::getHeight
        ));

        ElementUtils.registerElementSupport(EntryListWidgetAccessor.class, ElementSupport.fromLambda(
            EntryListWidgetAccessor::getLeft,
            EntryListWidgetAccessor::getTop,
            EntryListWidgetAccessor::getWidth,
            EntryListWidgetAccessor::getHeight
        ));

        ElementUtils.registerElementSupport(EntryListWidget.Entry.class, ElementSupport.fromLambda(
            w -> {
                var list = ((EntryListWidgetEntryAccessor) w).getParentList();
                return list.getRowLeft();
            },
            w -> {
                var list = ((EntryListWidgetEntryAccessor) w).getParentList();
                return ((EntryListWidgetAccessor) list).callGetRowTop(list.children().indexOf(w));
            },
            w -> {
                var list = ((EntryListWidgetEntryAccessor) w).getParentList();
                return list.getRowWidth();
            },
            w -> {
                var list = ((EntryListWidgetEntryAccessor) w).getParentList();
                return ((EntryListWidgetAccessor) list).getItemHeight();
            }
        ));
    }
}
