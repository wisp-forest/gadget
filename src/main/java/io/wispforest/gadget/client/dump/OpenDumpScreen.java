package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.gui.BasedLabelComponent;
import io.wispforest.gadget.client.gui.LayoutCacheWrapper;
import io.wispforest.gadget.util.FileUtil;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.gadget.client.dump.handler.DrawPacketHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenDumpScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Screen parent;
    private final ProgressToast toast;
    private final List<DisplayedPacket> packets;
    private VerticalFlowLayout main;
    private final List<DumpedPacket> rawPackets;
    private FlowLayout infoButton;
    private double lastFps;

    private OpenDumpScreen(Screen parent, ProgressToast toast, Path path) throws IOException {
        this.parent = parent;
        this.toast = toast;
        this.packets = new ArrayList<>();

        toast.step(Text.translatable("message.gadget.progress.reading_packets"));
        try (var is = toast.loadWithProgress(path)) {
            rawPackets = PacketDumpReader.readAll(is);
        }
    }

    public static void openWithProgress(Screen parent, Path path) {
        ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_dump"));
        MinecraftClient client = MinecraftClient.getInstance();

        toast.follow(
            CompletableFuture.supplyAsync(() -> {
                try {
                    OpenDumpScreen screen = new OpenDumpScreen(parent, toast, path);

                    toast.step(Text.translatable("message.gadget.progress.building_screen"));
                    screen.init(client, parent.width, parent.height);

                    return screen;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .thenAcceptAsync(client::setScreen, client),
            true);
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

        this.main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(90), this.main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        MutableInt progress = new MutableInt();
        toast.followProgress(progress::getValue, rawPackets.size());

        for (var packet : rawPackets) {
            if (packet.isIgnored()) {
                progress.add(1);
                continue;
            }

            VerticalFlowLayout view = Containers.verticalFlow(Sizing.content(), Sizing.content());

            view
                .padding(Insets.of(5))
                .surface(Surface.outline(packet.color()))
                .margins(Insets.bottom(5));

            String name = ReflectionUtil.nameWithoutPackage(packet.packet().getClass());

            MutableText typeText = Text.literal(name);

            if (packet.channelId() != null)
                typeText.append(Text.literal(" " + packet.channelId())
                    .formatted(Formatting.GRAY));

            view.child(new BasedLabelComponent(typeText)
                .margins(Insets.bottom(3)));

            DrawPacketHandler.EVENT.invoker().onDrawPacket(packet, view);

            HorizontalFlowLayout fullRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

            fullRow
                .child(view)
                .horizontalAlignment(packet.outbound() ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

            packets.add(new DisplayedPacket(packet, new LayoutCacheWrapper<>(fullRow)));

            progress.add(1);
        }

        var searchBox = Components.textBox(Sizing.fill(95));
        searchBox.setChangedListener(this::rebuildWithSearch);

        rootComponent
            .child(searchBox)
            .child(scroll
                .child(this.main)
                .margins(Insets.top(5)));
        this.main.padding(Insets.of(15));

        rebuildWithSearch("");

        VerticalFlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        infoButton = Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(16))
            .child(Components.label(Text.translatable("text.gadget.info"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            );

        infoButton.cursorStyle(CursorStyle.HAND);

        infoButton.mouseEnter().subscribe(
            () -> infoButton.surface(Surface.flat(0x80ffffff)));

        infoButton.mouseLeave().subscribe(
            () -> infoButton.surface(Surface.BLANK));

        sidebar
            .child(infoButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(2));

        rootComponent.child(sidebar);

        toast.step(Text.translatable("message.gadget.progress.mounting_components"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        lastFps = (6 * lastFps + 1000.f / (client.getLastFrameDuration() * 50)) / 7;
        if (infoButton.isInBoundingBox(mouseX, mouseY)) {
            List<Text> tooltip = new ArrayList<>();

            tooltip.add(Text.translatable("text.gadget.info.fps", FileUtil.formatDouble(lastFps)));

            var list = new ArrayList<Component>();
            uiAdapter.rootComponent.collectChildren(list);
            tooltip.add(Text.translatable("text.gadget.info.total_components", list.size()));

            infoButton.tooltip(tooltip);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void rebuildWithSearch(String searchText) {
        List<SearchWord> words = SearchWord.parseSearch(searchText);
        List<Component> neededComponents = new ArrayList<>();

        main.clearChildren();

        outer:
        for (var packet : packets) {
            String relevantText = packet.packet.searchText();

            for (var word : words) {
                if (!word.matches(relevantText))
                    continue outer;
            }

            neededComponents.add(packet.component);
        }

        main.children(neededComponents);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    record DisplayedPacket(DumpedPacket packet, Component component) { }
}
