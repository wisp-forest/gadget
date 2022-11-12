package io.wispforest.gadget.client.gui.inspector;

import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.util.Drawer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
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

    private int childAtOffset = 0;

    private VanillaInspector() {

    }

    public static VanillaInspector get(Screen screen) {
        return ALL.get(screen);
    }

    public static void init() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            VanillaInspector inspector = ALL.computeIfAbsent(screen, unused -> new VanillaInspector());

            ScreenEvents.afterRender(screen).register(inspector::drawInspector);
            ScreenKeyboardEvents.afterKeyPress(screen).register(inspector::keyPressed);
            ScreenEvents.remove(screen).register(ALL::remove);
            ScreenMouseEvents.allowMouseScroll(screen).register(inspector::mouseScrolled);
        });
    }

    private boolean mouseScrolled(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!enabled) return true;
        if (!Screen.hasShiftDown()) return true;

        childAtOffset += verticalAmount;
        if (childAtOffset < 0) childAtOffset = 0;

        return false;
    }

    public static void dumpWidgetTree(Screen screen) {
        StringBuilder sb = new StringBuilder();
        for (var parent : ElementUtils.listRootElements(screen))
            writeWidgetTree(parent, 0, sb);
        Gadget.LOGGER.info("Widget tree for screen:\n{}", sb);
    }

    private static void writeWidgetTree(Element element, int indent, StringBuilder sb) {
        sb.append(" ".repeat(indent));
        sb.append(ReflectionUtil.nameWithoutPackage(element.getClass()));
        sb.append(" ");
        sb.append(ElementUtils.x(element));
        sb.append(",");
        sb.append(ElementUtils.y(element));
        sb.append(" (");
        sb.append(ElementUtils.width(element));
        sb.append(",");
        sb.append(ElementUtils.height(element));
        sb.append(")\n");

        if (element instanceof ParentElement parent) {
            for (var child : parent.children()) {
                writeWidgetTree(child, indent + 1, sb);
            }
        }
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
    public void drawInspector(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
        if (!enabled) return;

        RenderSystem.disableDepthTest();
        var client = MinecraftClient.getInstance();
        var textRenderer = client.textRenderer;

        var parents = ElementUtils.listRootElements(screen);
        var children = new ArrayList<Element>();

        for (var parent : parents) {
            ElementUtils.collectChildren(parent, children);
        }

        if (onlyHovered) {
            children.removeIf(el -> !ElementUtils.inBoundingBox(el, mouseX, mouseY));

            childAtOffset = Math.min(childAtOffset, children.size() - 1);

            if (!children.isEmpty()) {
                var selected = children.get(childAtOffset);
                children.clear();
                children.add(selected);
            }
        }

        if (children.isEmpty())
            childAtOffset = 0;

        for (var child : children) {
            if (!ElementUtils.isVisible(child)) continue;
            if (ElementUtils.x(child) == -1) continue;

            matrices.translate(0, 0, 1000);

            Drawer.drawRectOutline(matrices, ElementUtils.x(child), ElementUtils.y(child), ElementUtils.width(child), ElementUtils.height(child), 0xFF3AB0FF);

            if (onlyHovered) {

                int inspectorX = ElementUtils.x(child) + 1;
                int inspectorY = ElementUtils.y(child) + ElementUtils.height(child) + 1;
                int inspectorHeight = textRenderer.fontHeight * 2 + 4;

                if (inspectorY > client.getWindow().getScaledHeight() - inspectorHeight) {
                    inspectorY -= ElementUtils.height(child) + inspectorHeight + 1;
                    if (inspectorY < 0) inspectorY = 1;
                }

                final var nameText = Text.of(ReflectionUtil.nameWithoutPackage(child.getClass()));
                final var descriptor = Text.literal(ElementUtils.x(child) + "," + ElementUtils.y(child) + " (" + ElementUtils.width(child) + "," + ElementUtils.height(child) + ")");

                int width = Math.max(textRenderer.getWidth(nameText), textRenderer.getWidth(descriptor));
                Drawer.fill(matrices, inspectorX, inspectorY, inspectorX + width + 3, inspectorY + inspectorHeight, 0xA7000000);
                Drawer.drawRectOutline(matrices, inspectorX, inspectorY, width + 3, inspectorHeight, 0xA7000000);

                textRenderer.draw(matrices, nameText,
                    inspectorX + 2, inspectorY + 2, 0xFFFFFF);
                textRenderer.draw(matrices, descriptor,
                    inspectorX + 2, inspectorY + textRenderer.fontHeight + 2, 0xFFFFFF);
            }
            matrices.translate(0, 0, -1000);
        }

        RenderSystem.enableDepthTest();
    }
}
