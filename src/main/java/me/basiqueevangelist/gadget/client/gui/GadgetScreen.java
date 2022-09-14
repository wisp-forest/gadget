package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import me.basiqueevangelist.gadget.client.dump.OpenDumpScreen;
import me.basiqueevangelist.gadget.client.dump.PacketDumper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class GadgetScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Screen parent;

    public GadgetScreen(Screen parent) {
        this.parent = parent;
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
            .scrollbarColor(0xA0FFFFFF);

        rootComponent.child(scroll.child(main));
        main.padding(Insets.of(15));

        try {
            if (!Files.exists(PacketDumper.DUMP_DIR))
                Files.createDirectories(PacketDumper.DUMP_DIR);

            try (var dumps = Files.list(PacketDumper.DUMP_DIR)) {
                for (var dump : (Iterable<Path>)dumps::iterator) {
                    String filename = dump.getFileName().toString();

                    HorizontalFlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());
                    row.child(Components.label(
                        Text.literal("d")
                            .formatted(Formatting.DARK_RED))
                        .margins(Insets.right(5)))
                        .child(Components.label(Text.literal(filename + " ")))
                        .padding(Insets.bottom(2));

                    LabelComponent openLabel = Components.label(Text.translatable("text.gadget.open"));

                    openLabel.cursorStyle(CursorStyle.HAND);
                    GuiUtil.hoverBlue(openLabel);
                    openLabel.mouseDown().subscribe((mouseX, mouseY, button) -> {
                        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                        try (InputStream is = Files.newInputStream(dump)) {
                            client.setScreen(new OpenDumpScreen(this, is));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        return true;
                    });

                    row.child(openLabel);
                    main.child(row);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
