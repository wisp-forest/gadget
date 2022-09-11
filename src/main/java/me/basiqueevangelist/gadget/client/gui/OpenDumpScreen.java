package me.basiqueevangelist.gadget.client.gui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.VerticalFlowLayout;
import io.wispforest.owo.ui.core.*;
import me.basiqueevangelist.gadget.client.dump.DumpedPacket;
import me.basiqueevangelist.gadget.client.dump.PacketDumpReader;
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
import java.util.List;

public class OpenDumpScreen extends BaseOwoScreen<VerticalFlowLayout> {
    private final Screen parent;
    private final List<DumpedPacket> packets;

    public OpenDumpScreen(Screen parent, InputStream file) throws IOException {
        this.parent = parent;
        this.packets = PacketDumpReader.readAll(file);
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
        ScrollContainer<VerticalFlowLayout> scroll = Containers.verticalScroll(Sizing.fill(95), Sizing.fill(100), main);

        rootComponent.child(scroll.child(main));
        main.padding(Insets.of(15));

        for (var packet : packets) {
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

            view.child(Components.label(typeText));

            FieldDataIsland island = new FieldDataIsland();
            island.targetObject(packet.packet(), false);

            view.child(island.mainContainer());

            HorizontalFlowLayout fullRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

            fullRow
                .child(view)
                .horizontalAlignment(packet.outbound() ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

            main.child(fullRow);
        }

    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
