package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.gui.BasedVerticalFlowLayout;
import io.wispforest.gadget.util.NumberUtil;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DumpStatsScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Map<String, PacketTypeData> packetTypes = new HashMap<>();
    private final Screen parent;
    private final List<ProcessedDumpedPacket> packets;
    private int totalSize = 0;

    public DumpStatsScreen(Screen parent, List<ProcessedDumpedPacket> packets) {
        this.parent = parent;
        this.packets = packets;
        for (var packet : packets) {
            var type = packetTypes.computeIfAbsent(packet.searchText(), unused -> new PacketTypeData());
            type.total += 1;
            type.size += packet.packet().size();
            totalSize += packet.packet().size();
        }
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

        VerticalFlowLayout main = new BasedVerticalFlowLayout(Sizing.fill(100), Sizing.content());
        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(90), main)
            .scrollbar(ScrollContainer.Scrollbar.flat(Color.ofArgb(0xA0FFFFFF)));

        packetTypes
            .entrySet()
            .stream()
            .sorted(Comparator.comparing(x -> -x.getValue().size))
            .forEachOrdered(x -> {
                double sizePercent = (double) x.getValue().size / totalSize;
                double totalPercent = (double) x.getValue().total / packets.size();

                MutableText total = Text.literal(x.getKey())
                    .append(Text.literal(" " + x.getValue().total + " packets,")
                        .formatted(Formatting.GRAY))
                    .append(Text.literal(" " + NumberUtil.formatFileSize(x.getValue().size) + " total")
                        .formatted(Formatting.GRAY))
                    .append(Text.literal("\n  " + NumberUtil.formatPercent(sizePercent) + " of size"))
                    .append(Text.literal("\n  " + NumberUtil.formatPercent(totalPercent) + " of packets"));

                main.child(Components.label(total)
                    .margins(Insets.bottom(3)));
            });

        rootComponent
            .child(scroll);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    private static class PacketTypeData {
        private int total;
        private int size;
    }
}
