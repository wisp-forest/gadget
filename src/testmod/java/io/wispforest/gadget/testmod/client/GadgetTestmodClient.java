package io.wispforest.gadget.testmod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.Screen;

public class GadgetTestmodClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (Screen.hasShiftDown()) {
                context.matrixStack().pop();
            }
        });
    }
}
