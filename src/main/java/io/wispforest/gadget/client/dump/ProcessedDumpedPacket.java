package io.wispforest.gadget.client.dump;

import io.wispforest.gadget.client.dump.handler.DrawPacketHandler;
import io.wispforest.gadget.client.dump.handler.SearchTextPacketHandler;
import io.wispforest.gadget.client.gui.BasedLabelComponent;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.client.gui.LayoutCacheWrapper;
import io.wispforest.gadget.dump.GadgetReadErrorPacket;
import io.wispforest.gadget.dump.GadgetWriteErrorPacket;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.container.*;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.ref.SoftReference;

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
        if (component == null || component.get() == null) {
            FlowLayout view = Containers.verticalFlow(Sizing.content(), Sizing.content());

            view
                .padding(Insets.of(5))
                .surface(Surface.outline(packet.color()))
                .margins(Insets.bottom(5));



            MutableText typeText = Text.literal("");

            if (packet.packet() instanceof GadgetReadErrorPacket errorPacket) {
                typeText.append(Text.translatable("text.gadget.packet_read_error", errorPacket.packetId()));
            } else if (packet.packet() instanceof GadgetWriteErrorPacket errorPacket) {
                typeText.append(Text.translatable("text.gadget.packet_write_error", errorPacket.packetId()));
            } else {
                typeText.append(ReflectionUtil.nameWithoutPackage(packet.packet().getClass()));

                if (packet.channelId() != null)
                    typeText.append(Text.literal(" " + packet.channelId())
                        .formatted(Formatting.GRAY));
            }

            view.child(new BasedLabelComponent(typeText)
                .margins(Insets.bottom(3)));

            DrawPacketHandler.EVENT.invoker().onDrawPacket(packet, view);

            if (!packet.drawErrors().isEmpty()
             || !packet.searchTextErrors().isEmpty()
             || packet.packet() instanceof GadgetReadErrorPacket
             || packet.packet() instanceof GadgetWriteErrorPacket) {
                CollapsibleContainer errors = Containers.collapsible(
                    Sizing.content(),
                    Sizing.content(),
                    Text.translatable("text.gadget.packet_errors"),
                    false
                );

                errors
                    .padding(Insets.of(2))
                    .margins(Insets.bottom(5));

                ((FlowLayout) errors.children().get(0))
                    .padding(Insets.of(2, 2, 2, 0));

                if (packet.packet() instanceof GadgetReadErrorPacket error) {
                    errors.child(GuiUtil.showException(error.exception())
                        .margins(Insets.bottom(2)));
                }

                if (packet.packet() instanceof GadgetWriteErrorPacket error) {
                    errors.child(GuiUtil.showExceptionText(error.exceptionText())
                        .margins(Insets.bottom(2)));
                }

                for (var e : packet.searchTextErrors()) {
                    errors.child(GuiUtil.showException(e)
                        .margins(Insets.bottom(2)));
                }

                for (var e : packet.drawErrors()) {
                    errors.child(GuiUtil.showException(e)
                        .margins(Insets.bottom(2)));
                }

                view.child(1, errors);
            }

            FlowLayout fullRow = Containers.horizontalFlow(Sizing.fill(100), Sizing.content());

            fullRow
                .child(view)
                .horizontalAlignment(packet.outbound() ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);

            component = new SoftReference<>(new LayoutCacheWrapper<>(fullRow));
        }

        return component.get();
    }

    public String searchText() {
        if (searchText == null) {
            StringBuilder sb = new StringBuilder();

            SearchTextPacketHandler.EVENT.invoker().onCreateSearchText(packet, sb);

            this.searchText = sb.toString();
        }

        return searchText;
    }
}
