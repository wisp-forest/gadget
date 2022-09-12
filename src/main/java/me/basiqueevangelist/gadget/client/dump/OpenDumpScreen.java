package me.basiqueevangelist.gadget.client.dump;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import me.basiqueevangelist.gadget.util.NetworkUtil;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class OpenDumpScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Screen parent;
    private final List<DisplayedPacket> packets;
    private VerticalFlowLayout main;

    public OpenDumpScreen(Screen parent, InputStream file) throws IOException {
        this.parent = parent;
        this.packets = new ArrayList<>();

        var rawPackets = PacketDumpReader.readAll(file);


        for (var packet : rawPackets) {
            VerticalFlowLayout view = Containers.verticalFlow(Sizing.content(), Sizing.content());

            view
                .padding(Insets.of(5))
                .surface(Surface.outline(packet.color()))
                .margins(Insets.bottom(5));

            MutableText typeText = Text.literal(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));
            Identifier channel = NetworkUtil.getChannelOrNull(packet.packet());

            if (channel != null)
                typeText.append(Text.literal(" " + channel)
                    .formatted(Formatting.GRAY));

            view.child(Components.label(typeText)
                .margins(Insets.bottom(3)));

            DrawPacketHandler.EVENT.invoker().onDrawPacket(packet, view);

            HorizontalFlowLayout fullRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

            fullRow
                .child(view)
                .horizontalAlignment(packet.outbound() ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

            packets.add(new DisplayedPacket(packet, fullRow));
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

        this.main = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(90), this.main);

        var searchBox = Components.textBox(Sizing.fill(95));
        searchBox.setChangedListener(this::rebuildWithSearch);

        rootComponent
            .child(searchBox)
            .child(scroll
                .child(this.main)
                .margins(Insets.top(5)));
        this.main.padding(Insets.of(15));

        rebuildWithSearch("");
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

            neededComponents.add(packet.fullRow);
        }

        main.children(neededComponents);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    record DisplayedPacket(DumpedPacket packet, HorizontalFlowLayout fullRow) { }
}
