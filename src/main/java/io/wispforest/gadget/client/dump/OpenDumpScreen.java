package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.BasedSliderComponent;
import io.wispforest.gadget.client.gui.BasedVerticalFlowLayout;
import io.wispforest.gadget.client.gui.SaveFilePathComponent;
import io.wispforest.gadget.dump.read.DumpedPacket;
import io.wispforest.gadget.dump.read.PacketDumpReader;
import io.wispforest.gadget.util.*;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import io.wispforest.owo.ui.util.UISounds;
import io.wispforest.owo.util.Observable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OpenDumpScreen extends BaseOwoScreen<FlowLayout> {
    private final Screen parent;
    private ProgressToast toast;
    private final PacketDumpReader reader;
    private final Path path;
    private FlowLayout main;
    private FlowLayout infoButton;
    private BasedSliderComponent timeSlider;
    private TextBoxComponent searchBox;
    private CancellationTokenSource currentSearchToken = null;

    private OpenDumpScreen(Screen parent, ProgressToast toast, PacketDumpReader reader, Path path) {
        this.parent = parent;
        this.toast = toast;
        this.reader = reader;
        this.path = path;
    }

    public static void openWithProgress(Screen parent, Path path) {
        ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_dump"));
        MinecraftClient client = MinecraftClient.getInstance();

        toast.follow(
            CompletableFuture.supplyAsync(() -> {
                try {
                    toast.step(Text.translatable("message.gadget.progress.reading_packets"));
                    var reader = new PacketDumpReader(path, toast);

                    toast.step(Text.translatable("message.gadget.progress.building_screen"));
                    OpenDumpScreen screen = new OpenDumpScreen(parent, toast, reader, path);
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
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, Containers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .surface(Surface.VANILLA_TRANSLUCENT);

        this.main = new BasedVerticalFlowLayout(Sizing.fill(100), Sizing.content());
        ScrollContainer<FlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(90), this.main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        searchBox = Components.textBox(Sizing.fill(95));
        searchBox.onChanged().subscribe(text -> rebuild(text, currentTime()));
        searchBox.margins(Insets.bottom(3));

        rootComponent.keyPress().subscribe((keyCode, scanCode, modifiers) -> {
            if (keyCode != GLFW.GLFW_KEY_F || (modifiers & GLFW.GLFW_MOD_CONTROL) == 0)
                return false;

            uiAdapter.rootComponent.focusHandler().focus(
                searchBox,
                Component.FocusSource.MOUSE_CLICK
            );

            return true;
        });

        timeSlider = new BasedSliderComponent(Sizing.fill(95));
        timeSlider
            .tooltipFactory(value -> Text.of(
                DurationFormatUtils.formatDurationHMS(currentTime(value) - reader.startTime())
            ))
            .message(unused -> Text.of(
                DurationFormatUtils.formatDurationHMS(currentTime() - reader.startTime())
            ));
        timeSlider.onChanged().subscribe(value -> {
            rebuild(searchBox.getText(), currentTime());
        });

        rootComponent
            .child(searchBox);

        if (reader.endTime() > reader.startTime())
            rootComponent.child(timeSlider);

        rootComponent
            .child(scroll
                .child(this.main)
                .margins(Insets.top(5)));

        this.main.padding(Insets.of(15));

        rebuild("", reader.startTime());

        FlowLayout sidebar = Containers.verticalFlow(Sizing.content(), Sizing.content());

        infoButton = new FlowLayout(Sizing.fixed(16), Sizing.fixed(16), FlowLayout.Algorithm.VERTICAL) {
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
                    Text.translatable("text.gadget.info.fps", client.getCurrentFps())
                        .asOrderedText()));

                tooltip.add(TooltipComponent.of(Text.translatable("text.gadget.info.total_components", totalComponents).asOrderedText()));

                tooltip.add(TooltipComponent.of(Text.translatable("text.gadget.info.total_packets", reader.packets().size()).asOrderedText()));

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

        FlowLayout statsButton = Containers.verticalFlow(Sizing.fixed(16), Sizing.fixed(16));

        statsButton
            .child(Components.label(Text.translatable("text.gadget.stats"))
                .verticalTextAlignment(VerticalAlignment.CENTER)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .positioning(Positioning.absolute(5, 4))
                .cursorStyle(CursorStyle.HAND)
            )
            .cursorStyle(CursorStyle.HAND)
            .tooltip(Text.translatable("text.gadget.stats.tooltip"));

        statsButton.mouseEnter().subscribe(
            () -> statsButton.surface(Surface.flat(0x80ffffff)));

        statsButton.mouseLeave().subscribe(
            () -> statsButton.surface(Surface.BLANK));

        statsButton.mouseDown().subscribe((mouseX, mouseY, button) -> {
            if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

            UISounds.playInteractionSound();

            ProgressToast toast = ProgressToast.create(Text.translatable("message.gadget.loading_dump_stats"));

            toast.follow(
                CompletableFuture.supplyAsync(() -> {
                        toast.step(Text.translatable("message.gadget.progress.calculating_data"));

                        return new DumpStatsScreen(parent, reader, toast);
                    })
                    .thenAcceptAsync(client::setScreen, client),
                true);

            return true;
        });

        sidebar
            .child(infoButton)
            .child(statsButton)
            .positioning(Positioning.absolute(0, 0))
            .padding(Insets.of(2));

        rootComponent.child(sidebar);

        toast.step(Text.translatable("message.gadget.progress.mounting_components"));
    }

    private long currentTime(double value) {
        return (long) MathHelper.lerp(value, reader.startTime(), reader.endTime());
    }

    private long currentTime() {
        return currentTime(timeSlider.value());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_E && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            // Export.
            FlowLayout exportModal = Containers.verticalFlow(Sizing.content(), Sizing.content());

            exportModal
                .surface(Surface.DARK_PANEL)
                .padding(Insets.of(8));

            exportModal.child(Components.label(Text.translatable("text.gadget.export.packet_dump"))
                .margins(Insets.bottom(4)));

            SaveFilePathComponent savePath = new SaveFilePathComponent(
                I18n.translate("text.gadget.export.packet_dump"),
                path.toString() + ".txt")
                .pattern("*.txt")
                .filterDescription("Plain Text file");

            LabelComponent progressLabel = Components.label(Text.translatable("text.gadget.export.gather_progress", 0));
            Observable<Integer> count = Observable.of(0);

            ReactiveUtils.throttle(count, TimeUnit.MILLISECONDS.toNanos(100), client)
                .observe(progress ->
                    progressLabel.text(Text.translatable("text.gadget.export.gather_progress", progress)));

            CancellationTokenSource tokSource = new CancellationTokenSource();

            CompletableFuture<List<DumpedPacket>> collected = CompletableFuture.supplyAsync(() ->
                reader.collectFor(searchBox.getText(), currentTime(), Integer.MAX_VALUE, count::set, tokSource.token()));

            exportModal.child(Containers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(Components.label(Text.translatable("text.gadget.export.output_path")))
                .child(savePath)
                .verticalAlignment(VerticalAlignment.CENTER)
            );

            exportModal.child(progressLabel);

            var button = Components.button(Text.translatable("text.gadget.export.export_button"), b -> {
                tokSource.token().throwIfCancelled();

                try {
                    Path nioPath = Path.of(savePath.path().get());
                    var os = Files.newOutputStream(nioPath);
                    var bos = new BufferedOutputStream(os);
                    FormattedDumper dumper = new FormattedDumper(new PrintStream(bos));

                    ProgressToast toast = ProgressToast.create(Text.translatable("text.gadget.export.exporting_packet_dump"));
                    dumper.write(0, "Packet dump " + path.getFileName().toString());
                    MutableLong progress = new MutableLong();

                    toast.force();
                    toast.followProgress(progress::getValue, collected.join().size());
                    toast.follow(CompletableFuture.runAsync(() -> {
                        for (var packet : collected.join()) {
                            reader.dumpPacketToText(packet, dumper, 0);
                            progress.increment();
                            tokSource.token().throwIfCancelled();
                        }
                    }), false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            button.active(false);
            collected.whenCompleteAsync((r, t) -> {
                if (t != null) {
                    exportModal.child(exportModal.children().size() - 1, Components.label(Text.translatable("text.gadget.export.error")));
                    Gadget.LOGGER.error("Error occured while gathering packets for export", t);
                } else {
                    button.active(true);
                }
            }, client);

            exportModal.child(button);

            uiAdapter.rootComponent.child(new OverlayContainer<>(exportModal) {
                @Override
                public void dismount(DismountReason reason) {
                    super.dismount(reason);

                    if (reason != DismountReason.REMOVED) return;

                    // breh
                    tokSource.cancel();
                }
            });

            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            for (Component rootChild : uiAdapter.rootComponent.children()) {
                if (rootChild instanceof OverlayContainer<?> overlay && overlay.closeOnClick()) {
                    overlay.remove();
                    return true;
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void rebuild(String searchText, long time) {
        if (currentSearchToken != null)
            currentSearchToken.cancel();

        currentSearchToken = new CancellationTokenSource();
        CancellationToken token = currentSearchToken.token();

        CompletableFuture.supplyAsync(() -> {
            List<Component> neededComponents = new ArrayList<>();

            for (var packet : reader.collectFor(searchText, time, 300, unused -> {}, token)) {
                token.throwIfCancelled();
                neededComponents.add(packet.get(RenderedPacketComponent.KEY).component());
            }

            return neededComponents;
        })
            .thenAcceptAsync(components -> {
                main.configure(a -> {
                    main.clearChildren();
                    main.children(components);
                });
            }, client)
            .whenComplete((r, t) -> {
                if (t != null) {
                    if (t.getCause() instanceof CancellationException) return;

                    Gadget.LOGGER.error("Search failed!", t);
                }
            });
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
