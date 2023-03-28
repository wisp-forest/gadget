package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.ServerData;
import io.wispforest.gadget.network.GadgetNetworking;
import io.wispforest.gadget.network.packet.c2s.ReplaceStackC2SPacket;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class StackNbtDataScreen extends BaseOwoScreen<FlowLayout> {
    private final NbtDataIsland island;
    private final HandledScreen<?> parent;

    public StackNbtDataScreen(HandledScreen<?> parent, Slot slot) {
        var stack = slot.getStack();
        Consumer<NbtCompound> reloader = null;

        if (ServerData.canReplaceStacks()) {
            reloader = newNbt -> {
                stack.setNbt(newNbt);

                if (parent instanceof CreativeInventoryScreen) {
                    // Let it handle it.
                    return;
                }

                GadgetNetworking.CHANNEL.clientHandle().send(new ReplaceStackC2SPacket(slot.id, stack));
            };
        }

        NbtCompound tag = stack.getNbt();

        if (tag == null) tag = new NbtCompound();

        this.parent = parent;
        this.island = new NbtDataIsland(tag, reloader);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);


        FlowLayout main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        rootComponent.child(scroll.child(main));

        main
            .padding(Insets.of(15));

        main.child(island);

        FlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        var addButton = Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(16))
            .child(Components.label(Text.literal("+"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );

        addButton
            .cursorStyle(CursorStyle.HAND);

        addButton.mouseEnter().subscribe(
            () -> addButton.surface(Surface.flat(0x80ffffff)));

        addButton.mouseLeave().subscribe(
            () -> addButton.surface(Surface.BLANK));

        addButton.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            island.typeSelector(
                (int) (addButton.x() + mouseX),
                (int) (addButton.y() + mouseY),
                type -> island.child(new KeyAdderWidget(island, NbtPath.EMPTY, type, unused -> true)));

            return true;
        });

        sidebar
            .child(addButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(5));

        rootComponent.child(sidebar);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
