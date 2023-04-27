package io.wispforest.gadget.client.dump.handler;

import io.wispforest.gadget.client.field.FieldDataIsland;
import io.wispforest.gadget.client.gui.GuiUtil;
import io.wispforest.gadget.dump.fake.GadgetReadErrorPacket;
import io.wispforest.gadget.dump.fake.GadgetWriteErrorPacket;
import io.wispforest.gadget.dump.read.handler.PacketHandlers;
import io.wispforest.gadget.dump.read.handler.PacketUnwrapper;
import io.wispforest.gadget.field.LocalFieldDataSource;
import io.wispforest.gadget.util.NetworkUtil;
import io.wispforest.gadget.util.ReflectionUtil;
import io.wispforest.owo.ui.component.Components;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ClientPacketHandlers {
    private ClientPacketHandlers() {

    }

    public static void init() {
        OwoSupport.init();
        MinecraftSupport.init();
        FapiSupport.init();

        PacketRenderer.EVENT.register((packet, view, errSink) -> {
            var unwrapped = PacketUnwrapper.EVENT.invoker().tryUnwrap(packet, errSink);

            if (unwrapped == null) return false;

            MutableText headText = Text.literal(ReflectionUtil.nameWithoutPackage(unwrapped.packet().getClass()));

            if (unwrapped.packetId().isPresent()) {
                headText.append(Text.literal(" #" + unwrapped.packetId())
                    .formatted(Formatting.GRAY));
            }

            view.child(Components.label(headText));

            FieldDataIsland island = new FieldDataIsland(
                new LocalFieldDataSource(unwrapped.packet(), false),
                true,
                false
            );

            view.child(island.mainContainer());

            return true;
        });

        PacketRenderer.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, PacketHandlers.LAST_PHASE);
        PacketRenderer.EVENT.register(PacketHandlers.LAST_PHASE, (packet, view, errSink) -> {
            if (packet.packet() instanceof GadgetReadErrorPacket errorPacket) {
                view.child(GuiUtil.hexDump(errorPacket.data(), true));
                return true;
            }

            if (packet.packet() instanceof GadgetWriteErrorPacket) return true;

            if (packet.channelId() != null) {
                var buf = NetworkUtil.unwrapCustom(packet.packet());
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                view.child(GuiUtil.hexDump(bytes, true));
                return true;
            }

            FieldDataIsland island = new FieldDataIsland(
                new LocalFieldDataSource(packet.packet(), false),
                true,
                false
            );

            view.child(island.mainContainer());

            return true;
        });
    }
}
