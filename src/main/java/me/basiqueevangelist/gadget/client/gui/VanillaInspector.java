package me.basiqueevangelist.gadget.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import io.wispforest.owo.ui.util.Drawer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public class VanillaInspector {
    private static final Map<Screen, VanillaInspector> ALL = new WeakHashMap<>();

    private boolean enabled;
    private boolean onlyHovered = true;

    private VanillaInspector() {

    }

    public static VanillaInspector get(Screen screen) {
        return ALL.get(screen);
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            VanillaInspector inspector = get(screen);

            if (inspector == null)
                inspector = new VanillaInspector();

            ALL.put(screen, inspector);

            ScreenEvents.afterRender(screen).register(inspector::drawInspector);
        });
    }

    public void keyPressed(Screen screen, int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_LEFT_SHIFT) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                enabled = !enabled;
            } else if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
                onlyHovered = !onlyHovered;
            }
        }
    }

    // Mostly copied (and modified) from Drawer$DebugDrawer#drawInspector
    private void drawInspector(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (!enabled) return;

        RenderSystem.disableDepthTest();
        var textRenderer = MinecraftClient.getInstance().textRenderer;

        var children = new ArrayList<Element>();
        if (!onlyHovered) {
            GuiUtil.collectChildren(screen, children);
        } else if (GuiUtil.childAt(screen, mouseX, mouseY) != null) {
            children.add(GuiUtil.childAt(screen, mouseX, mouseY));
        }

        for (var child : children) {
            if (GuiUtil.x(child) == -1) continue;
            if (!GuiUtil.isVisible(child)) continue;
            if (child instanceof OwoUIAdapter<?>) continue;

            matrices.translate(0, 0, 1000);

            Drawer.drawRectOutline(matrices, GuiUtil.x(child), GuiUtil.y(child), GuiUtil.width(child), GuiUtil.height(child), 0xFF3AB0FF);

            if (onlyHovered) {
                textRenderer.draw(matrices, Text.of(child.getClass().getSimpleName()),
                    GuiUtil.x(child) + 1, GuiUtil.y(child) + GuiUtil.height(child) + 1, 0xFFFFFF);

                final var descriptor = Text.literal(GuiUtil.x(child) + "," + GuiUtil.y(child) + " (" + GuiUtil.width(child) + "," + GuiUtil.height(child) + ") ");
                textRenderer.draw(matrices, descriptor,
                    GuiUtil.x(child) + 1, GuiUtil.y(child) + GuiUtil.height(child) + textRenderer.fontHeight + 2, 0xFFFFFF);
            }
            matrices.translate(0, 0, -1000);
        }

        RenderSystem.enableDepthTest();
    }
}
