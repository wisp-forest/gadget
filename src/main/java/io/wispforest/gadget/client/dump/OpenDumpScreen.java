package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.gui.BasedSliderComponent;
import io.wispforest.gadget.client.gui.BasedVerticalFlowLayout;
import io.wispforest.gadget.util.FileUtil;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OpenDumpScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Screen parent;
    private ProgressToast toast;
    private final List<ProcessedDumpedPacket> packets;
    private VerticalFlowLayout main;
    private FlowLayout infoButton;
    private BasedSliderComponent timeSlider;
    private final long startTime;
    private final long endTime;

    private OpenDumpScreen(Screen parent, ProgressToast toast, Path path) throws IOException {
        this.parent = parent;
        this.toast = toast;
        this.packets = new ArrayList<>();

        toast.step(Text.translatable("message.gadget.progress.reading_packets"));
        List<DumpedPacket> rawPackets;
        try (var is = toast.loadWithProgress(path)) {
            if (path.toString().endsWith(".dump"))
                rawPackets = PacketDumpReader.readV0(is);
            else
                rawPackets = PacketDumpReader.readNew(is);
        }

        rawPackets.forEach(packet -> packets.add(new ProcessedDumpedPacket(packet)));

        startTime = rawPackets.get(0).sentAt();
        endTime = rawPackets.get(rawPackets.size() - 1).sentAt();
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
                    screen.toast = null;

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

        this.main = new BasedVerticalFlowLayout(Sizing.fill(100), Sizing.content());
        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(90), this.main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        var searchBox = Components.textBox(Sizing.fill(95));
        searchBox.setChangedListener(text -> rebuild(text, currentTime()));
        searchBox.margins(Insets.bottom(3));

        timeSlider = new BasedSliderComponent(Sizing.fill(95));
        timeSlider
            .tooltipFactory(value -> Text.of(
                DurationFormatUtils.formatDurationHMS(currentTime(value) - startTime)
            ))
            .message(unused -> Text.of(
                DurationFormatUtils.formatDurationHMS(currentTime() - startTime)
            ));
        timeSlider.onChanged(value -> {
            rebuild(searchBox.getText(), currentTime());
        });

        rootComponent
            .child(searchBox);

        if (endTime > startTime)
            rootComponent.child(timeSlider);

        rootComponent
            .child(scroll
                .child(this.main)
                .margins(Insets.top(5)));

        this.main.padding(Insets.of(15));

        rebuild("", startTime);

        VerticalFlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        infoButton = new VerticalFlowLayout(Sizing.fixed(16), Sizing.fixed(16)) {
            private int totalComponents = -1;
            private int frameNumber = 11;

            @Override
            public void drawTooltip(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
                frameNumber++;

                if (!this.shouldDrawTooltip(mouseX, mouseY)) return;

                if (frameNumber > 9) {
                    frameNumber = 0;

                    var list = new ArrayList<Component>();
                    uiAdapter.rootComponent.collectChildren(list);
                    totalComponents = list.size();
                }

                List<TooltipComponent> tooltip = new ArrayList<>();

                tooltip.add(TooltipComponent.of(
                    Text.translatable("text.gadget.info.fps", FileUtil.formatDouble((1000.f / (delta * 50))))
                        .asOrderedText()));

                tooltip.add(TooltipComponent.of(Text.translatable("text.gadget.info.total_components", totalComponents).asOrderedText()));

                tooltip.add(TooltipComponent.of(Text.translatable("text.gadget.info.total_packets", packets.size()).asOrderedText()));

                tooltip.add(TooltipComponent.of(Text.translatable("text.gadget.info.packets_on_screen", main.children().size()).asOrderedText()));

                Drawer.drawTooltip(matrices, mouseX, mouseY, tooltip);
            }
        };

        infoButton
            .child(Components.label(Text.translatable("text.gadget.info"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            )
            .cursorStyle(CursorStyle.HAND);

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

    private long currentTime(double value) {
        return (long) (startTime + (endTime - startTime) * value);
    }

    private long currentTime() {
        return currentTime(timeSlider.value());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    private void rebuild(String searchText, long time) {
        List<SearchWord> words = SearchWord.parseSearch(searchText);
        List<Component> neededComponents = new ArrayList<>();

        main.clearChildren();

        outer:
        for (var packet : packets) {
            if (packet.packet().sentAt() < time) continue;
            if (packet.packet().sentAt() > time && neededComponents.size() > 300) break;

            String relevantText = packet.searchText();

            for (var word : words) {
                if (!word.matches(relevantText))
                    continue outer;
            }

            neededComponents.add(packet.component());
        }

        main.children(neededComponents);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
