package io.wispforest.gadget.client.field;

import com.google.gson.stream.JsonWriter;
import io.wispforest.gadget.client.DialogUtil;
import io.wispforest.gadget.client.gui.search.SearchGui;
import io.wispforest.gadget.field.FieldDataSource;
import io.wispforest.gadget.field.LocalFieldDataSource;
import io.wispforest.gadget.network.*;
import io.wispforest.gadget.network.packet.c2s.OpenFieldDataScreenC2SPacket;
import io.wispforest.gadget.path.PathStep;
import io.wispforest.gadget.util.FormattedDumper;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UISounds;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FieldDataScreen extends BaseOwoScreen<FlowLayout> {
    private final InspectionTarget target;
    private final FieldDataSource dataSource;
    private final boolean isClient;
    public FieldDataIsland island;

    public FieldDataScreen(InspectionTarget target, boolean isClient, @Nullable FieldData rootData, @Nullable Map<PathStep, FieldData> initialFields) {
        this.target = target;
        this.isClient = isClient;

        if (!isClient)
            dataSource = new RemoteFieldDataSource(target, rootData, initialFields);
        else
            dataSource = new LocalFieldDataSource(target.resolve(MinecraftClient.getInstance().world), true);

        this.island = new FieldDataIsland(
            dataSource,
            false,
            true
        );
    }

    public InspectionTarget target() {
        return target;
    }

    public boolean isClient() {
        return isClient;
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout verticalFlowLayout) {
        verticalFlowLayout
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);


        FlowLayout main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());

        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        verticalFlowLayout.child(scroll.child(main));

        main
            .padding(Insets.of(15, 22, 15, 15));

        main.child(island.mainContainer());

        FlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

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
                GadgetNetworking.CHANNEL.clientHandle().send(new OpenFieldDataScreenC2SPacket(target));
            else
                client.setScreen(new FieldDataScreen(
                    target,
                    true,
                    null,
                    null
                ));

            return true;
        });

        sidebar
            .child(switchButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(5));

        SearchGui search = new SearchGui(scroll);
        verticalFlowLayout
            .child(search
                .positioning(Positioning.relative(0, 100)));
        verticalFlowLayout.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode != GLFW.GLFW_KEY_F || (modifiers & GLFW.GLFW_MOD_CONTROL) == 0)
                return false;

            uiAdapter.rootComponent.focusHandler().focus(
                search.searchBox(),
                Component.FocusSource.MOUSE_CLICK
            );

            return true;
        });

        verticalFlowLayout.child(sidebar);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_E && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            String path = DialogUtil.saveFileDialog(
                I18n.translate("text.gadget.export_field_dump"),
                FabricLoader.getInstance().getGameDir().toString() + "/",
                List.of("*.txt", "*.json"),
                "JSON or Plain Text file"
            );

            if (path != null) {
                try {
                    Path nioPath = Path.of(path);
                    if (path.endsWith(".txt")) {
                        var os = Files.newOutputStream(nioPath);
                        var bos = new BufferedOutputStream(os);
                        FormattedDumper dumper = new FormattedDumper(new PrintStream(bos));

                        dumper.write(0, "Field data of " + target);
                        island.dumpToText(dumper, 0, island.root(), 5)
                            .whenComplete((ignored1, ignored2) -> {
                                try {
                                    bos.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    } else if (path.endsWith(".json")) {
                        BufferedWriter bw = Files.newBufferedWriter(nioPath);
                        JsonWriter writer = new JsonWriter(bw);

                        var toast = ProgressToast.create(Text.translatable("text.gadget.exporting_field_dump"));

                        CompletableFuture<Void> future =
                            island.dumpToJson(
                                writer,
                                island.root(),
                                5,
                                f -> toast.step(Text.translatable("text.gadget.exporting.dumping_path", f.toString()))
                            )
                            .whenComplete((ignored1, ignored2) -> {
                                try {
                                    bw.close();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                        toast.follow(future, false);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public FieldDataSource dataSource() {
        return dataSource;
    }
}
