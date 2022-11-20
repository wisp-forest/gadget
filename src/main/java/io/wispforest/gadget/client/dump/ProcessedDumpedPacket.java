package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.dump.handler.ProcessPacketHandler;
import io.wispforest.gadget.client.gui.BasedLabelComponent;
import io.wispforest.gadget.client.gui.LayoutCacheWrapper;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.ref.SoftReference;
import java.util.Objects;

public final class ProcessedDumpedPacket {
    private final DumpedPacket packet;
    private SoftReference<Component> component;
    private String searchText;

    public ProcessedDumpedPacket(DumpedPacket packet) {
        this.packet = packet;
    }

    public DumpedPacket packet() {
        return packet;
    }

    public Component component() {
        if (component == null || component.get() == null) init();

        return component.get();
    }

    public String searchText() {
        if (searchText == null) init();

        return searchText;
    }

    private void init() {
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

        StringBuilder searchTextSb = new StringBuilder();

        ProcessPacketHandler.EVENT.invoker().onProcessPacket(packet, view, searchTextSb);

        HorizontalFlowLayout fullRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

        fullRow
            .child(view)
            .horizontalAlignment(packet.outbound() ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

        component = new SoftReference<>(new LayoutCacheWrapper<>(fullRow));
        this.searchText = searchTextSb.toString();
    }
}
