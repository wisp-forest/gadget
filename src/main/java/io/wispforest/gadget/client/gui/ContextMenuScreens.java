package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.DropdownComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

public class ContextMenuScreens {
    public static void init() {
        Layers.add(
            Containers::verticalFlow,
            instance -> {
                instance.adapter.rootComponent.child(new CringeComponent()
                    .id("gadget:cringe"));
            },
            SelectWorldScreen.class, MultiplayerScreen.class
        );
    }

    public static DropdownComponent contextMenuAt(Screen screen, double mouseX, double mouseY) {
        var instances = Layers.getInstances(screen);
        FlowLayout flow = null;

        for (var inst : instances) {
            if (inst.adapter.rootComponent.childById(CringeComponent.class, "gadget:cringe") != null) {
                flow = (FlowLayout) inst.adapter.rootComponent;
                break;
            }
        }

        if (flow == null) {
            throw new IllegalStateException("bruh");
        }

        return GuiUtil.contextMenu(flow, mouseX, mouseY);
    }
}
