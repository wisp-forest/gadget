package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class NotificationToast implements Toast {
    private final OwoUIAdapter<VerticalFlowLayout> adapter;
    private final MinecraftClient client = MinecraftClient.getInstance();

    public NotificationToast(Text headText, Text messageText) {
        this.adapter = OwoUIAdapter.createWithoutScreen(0, 0, 160, 32, Containers::verticalFlow);

        var root = this.adapter.rootComponent;

        root
            .child(Components.label(headText)
                .maxWidth(160)
                .horizontalTextAlignment(HorizontalAlignment.CENTER))
            .surface(Surface.VANILLA_TRANSLUCENT.and(Surface.outline(0xFF5800FF)))
            .allowOverflow(true)
            .horizontalAlignment(HorizontalAlignment.CENTER)
            .verticalAlignment(VerticalAlignment.CENTER)
            .padding(Insets.of(5));

        if (messageText != null)
            root.child(Components.label(messageText));

        this.adapter.inflateAndMount();

    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime) {
        this.adapter.render(matrices, 0, 0, client.getTickDelta());

        return startTime > 5000 ? Visibility.HIDE : Visibility.SHOW;
    }
}
