package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.client.DialogUtil;
import io.wispforest.gadget.client.dump.OpenDumpScreen;
import io.wispforest.gadget.client.dump.PacketDumper;
import io.wispforest.gadget.util.FileUtil;
import io.wispforest.gadget.util.NumberUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        rootComponent.child(scroll.child(main));
        main.padding(Insets.of(15));

        LabelComponent openOther = Components.label(Text.translatable("text.gadget.open_other_dump"));

        openOther.margins(Insets.bottom(4));
        GuiUtil.semiButton(openOther, () -> {
            String path = DialogUtil.openFileDialog(I18n.translate("text.gadget.open_other_dump"), null, List.of("*.dump"), "gadget network dumps", false);

            if (path != null) {
                OpenDumpScreen.openWithProgress(this, Path.of(path));
            }
        });

        main.child(openOther);

        try {
            if (!Files.exists(PacketDumper.DUMP_DIR))
                Files.createDirectories(PacketDumper.DUMP_DIR);

            for (var dump : FileUtil.listSortedByFileName(PacketDumper.DUMP_DIR)) {
                String filename = dump.getFileName().toString();

                HorizontalFlowLayout row = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

                Text labelText = Text.literal("")
                    .append(Text.literal("d ")
                        .formatted(Formatting.DARK_RED))
                    .append(Text.literal(filename + " "))
                    .append(Text.literal(NumberUtil.formatFileSize(Files.size(dump)) + " ")
                        .formatted(Formatting.GRAY));

                row.child(Components.label(labelText))
                    .padding(Insets.bottom(2));

                LabelComponent openLabel = Components.label(Text.translatable("text.gadget.open"));

                GuiUtil.semiButton(openLabel,
                    () -> OpenDumpScreen.openWithProgress(this, dump));

                row.child(openLabel);
                main.child(row);
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
