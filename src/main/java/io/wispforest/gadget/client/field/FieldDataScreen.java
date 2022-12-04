package io.wispforest.gadget.client.field;

import io.wispforest.gadget.client.gui.search.SearchGui;
import io.wispforest.gadget.network.*;
import io.wispforest.gadget.network.packet.c2s.RequestDataC2SPacket;
import io.wispforest.gadget.network.packet.c2s.SetNbtCompoundC2SPacket;
import io.wispforest.gadget.network.packet.c2s.SetPrimitiveC2SPacket;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.gadget.path.ObjectPath;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class FieldDataScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final InspectionTarget target;
    private final boolean isClient;
    public FieldDataIsland island;

    public FieldDataScreen(InspectionTarget target, boolean isClient) {
        this.target = target;
        this.isClient = isClient;

        this.island = new FieldDataIsland();

        this.island.generateSearchAnchors(true);

        if (isClient) {
            this.island.targetObject(target.resolve(MinecraftClient.getInstance().world), true);
        } else {
            this.island.pathRequester(path ->
                GadgetNetworking.CHANNEL.clientHandle().send(new RequestDataC2SPacket(this.target, path)));

            this.island.primitiveSetter((path, data) ->
                GadgetNetworking.CHANNEL.clientHandle().send(new SetPrimitiveC2SPacket(this.target, path, data)));

            this.island.nbtCompoundSetter((path, data) ->
                GadgetNetworking.CHANNEL.clientHandle().send(new SetNbtCompoundC2SPacket(this.target, path, data)));
        }
    }

    public InspectionTarget target() {
        return target;
    }

    public boolean isClient() {
        return isClient;
    }

    @Override
    protected @NotNull OwoUIAdapter<VerticalFlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(VerticalFlowLayout verticalFlowLayout) {
        verticalFlowLayout
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);


        VerticalFlowLayout main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        verticalFlowLayout.child(scroll.child(main));

        main
            .padding(Insets.of(15));

        main.child(island.mainContainer());

        VerticalFlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        var switchButton = Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(16))
            .child(Components.label(Text.translatable("text.gadget." + (isClient() ? "client" : "server") + "_current"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );

        switchButton
            .cursorStyle(CursorStyle.HAND)
            .tooltip(Text.translatable("text.gadget.switch_to_" + (isClient() ? "server" : "client")));

        switchButton.mouseEnter().subscribe(
            () -> switchButton.surface(Surface.flat(0x80ffffff)));

        switchButton.mouseLeave().subscribe(
            () -> switchButton.surface(Surface.BLANK));

        switchButton.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            if (isClient())
                GadgetNetworking.CHANNEL.clientHandle().send(new RequestDataC2SPacket(target, ObjectPath.EMPTY));
            else
                client.setScreen(new FieldDataScreen(target, true));

            return true;
        });

        sidebar
            .child(switchButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(5));

        SearchGui search = new SearchGui(scroll);
        verticalFlowLayout
            .child(search.createSearchComponent()
                .positioning(Positioning.relative(0, 100)));

        verticalFlowLayout.child(sidebar);
    }

    public void addFieldData(Map<ObjectPath, FieldData> data) {
        data.forEach(island::addFieldData);
        island.commitAdditions();
    }
}
