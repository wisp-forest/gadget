package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.component.LabelComponent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

public final class GuiUtil {
    private GuiUtil() {

    }

    public static void hoverBlue(LabelComponent label) {
        label.mouseEnter().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.BLUE)));

        label.mouseLeave().subscribe(
            () -> label.text(((MutableText) label.text()).formatted(Formatting.WHITE)));
    }
}
