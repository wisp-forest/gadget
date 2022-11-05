package io.wispforest.gadget.client.nbt;

import io.wispforest.gadget.client.ServerData;
import io.wispforest.gadget.network.GadgetNetworking;
import io.wispforest.gadget.network.packet.c2s.ReplaceStackC2SPacket;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class StackNbtDataScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final NbtDataIsland island;
    private final HandledScreen<?> parent;

    public StackNbtDataScreen(HandledScreen<?> parent, int slotId) {
        super();

        var stack = parent.getScreenHandler().slots.get(slotId).getStack();
        Consumer<NbtCompound> reloader = null;

        if (ServerData.ANNOUNCE_PACKET.canReplaceStacks()) {
            reloader = newNbt -> {
                stack.setNbt(newNbt);
                GadgetNetworking.CHANNEL.clientHandle().send(new ReplaceStackC2SPacket(slotId, stack));
            };
        }

        this.parent = parent;
        this.island = new NbtDataIsland(stack.getNbt(), reloader);
    }

    @Override
    protected @NotNull OwoUIAdapter<VerticalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(VerticalFlowLayout rootComponent) {
        rootComponent
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);


        VerticalFlowLayout main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        rootComponent.child(scroll.child(main));

        main
            .padding(Insets.of(15));

        main.child(island);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
