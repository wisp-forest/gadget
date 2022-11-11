package io.wispforest.gadget.client.gui;

import io.wispforest.gadget.util.ProgressInputStream;
import io.wispforest.gadget.util.ProgressToast;
import io.wispforest.owo.ui.component.BoxComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ProgressToastImpl implements Toast, ProgressToast {
    private final OwoUIAdapter<VerticalFlowLayout> adapter;
    private final MinecraftClient client = MinecraftClient.getInstance();
    private boolean attached = false;

    private final LabelComponent stepLabel;
    private final BoxComponent progressBox;
    private long stopTime = 0;
    private ProgressInputStream following = null;
    private int followingTotal = 0;

    public ProgressToastImpl(Text headText) {
        this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, 160, 32, Containers::verticalFlow);

        var root = this.adapter.rootComponent;

        root
            .child(Components.label(headText)
                .maxWidth(160)
                .horizontalTextAlignment(HorizontalAlignment.CENTER)
                .margins(Insets.bottom(0)))
            .child(stepLabel = Components.label(Text.empty())
                .maxWidth(160)
                .horizontalTextAlignment(HorizontalAlignment.CENTER))
            .child((progressBox = Components.box(Sizing.fixed(0), Sizing.fixed(3)))
                .color(Color.WHITE)
                .fill(true)
                .positioning(Positioning.absolute(0, 15)))
            .surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0xFF5800FF)))
            .allowOverflow(true)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .padding(Insets.of(10));

        this.adapter.inflateAndMount();
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        if (following == null) {
            progressBox.horizontalSizing(Sizing.fixed(0));
        } else {
            progressBox.horizontalSizing(Sizing.fixed(following.progress() * 140 / followingTotal));
        }

        this.adapter.render(matrices, 0, 0, client.getTickDelta());

        if (stopTime == -1)
            stopTime = startTime;

        if (stopTime == 0) {
            return Visibility.SHOW;
        } else {
            return startTime - stopTime > 2500 ? Visibility.HIDE : Visibility.SHOW;
        }
    }

    @Override
    public void step(Text text) {
        MinecraftClient.getInstance().execute(() -> {
            if (!attached) {
                MinecraftClient.getInstance().getToastManager().add(this);
                attached = true;
            }

            this.stepLabel.text(text);
            this.following = null;
        });

    }

    @Override
    public void followProgress(ProgressInputStream stream, int total) {
        this.following = stream;
        this.followingTotal = total;
    }

    @Override
    public void finish() {
        MinecraftClient.getInstance().execute(() -> {
            this.stepLabel.text(Text.translatable("message.gadget.progress.finished"));
            this.following = null;
            stopTime = -1;
        });
    }
}
