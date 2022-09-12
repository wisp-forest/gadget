package me.basiqueevangelist.gadget.client.dump;

import io.wispforest.owo.network.serialization.RecordSerializer;
import io.wispforest.owo.particles.systems.ParticleSystem;
import io.wispforest.owo.particles.systems.ParticleSystemController;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.util.VectorSerializer;
import me.basiqueevangelist.gadget.Gadget;
import me.basiqueevangelist.gadget.client.field.FieldDataIsland;
import me.basiqueevangelist.gadget.mixin.owo.OwoNetChannelAccessor;
import me.basiqueevangelist.gadget.mixin.owo.ParticleSystemAccessor;
import me.basiqueevangelist.gadget.util.NetworkUtil;
import me.basiqueevangelist.gadget.util.ReflectionUtil;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

@SuppressWarnings("UnstableApiUsage")
public final class DrawPacketHandlers {
    public static final Identifier LAST_PHASE = Gadget.id("last");

    private DrawPacketHandlers() {

    }

    public static void init() {
        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (packet.state() != NetworkState.PLAY) return false;

            Identifier channelId = NetworkUtil.getChannelOrNull(packet.packet());

            if (channelId == null) return false;

            OwoNetChannelAccessor channel = (OwoNetChannelAccessor) OwoNetChannelAccessor.getRegisteredChannels().get(channelId);

            if (channel == null) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int handlerId = buf.readVarInt();

            if (!packet.outbound())
                handlerId = -handlerId;

            RecordSerializer<?> serializer = channel.getSerializersByIndex().get(handlerId).getSerializer();

            Object unwrapped = serializer.read(buf);

            view.child(Components.label(Text.literal(ReflectionUtil.nameWithoutPackage(unwrapped.getClass()))));

            FieldDataIsland island = new FieldDataIsland();
            island.targetObject(unwrapped, false);

            view.child(island.mainContainer());

            return true;
        });

        DrawPacketHandler.EVENT.register((packet, view) -> {
            if (packet.state() != NetworkState.PLAY) return false;

            Identifier channelId = NetworkUtil.getChannelOrNull(packet.packet());

            if (channelId == null) return false;

            ParticleSystemController controller = ParticleSystemController.REGISTERED_CONTROLLERS.get(channelId);

            if (controller == null) return false;

            PacketByteBuf buf = NetworkUtil.unwrapCustom(packet.packet());
            int systemId = buf.readVarInt();
            Vec3d pos = VectorSerializer.read(buf);

            view.child(Components.label(Text.translatable("text.gadget.particle_system", systemId, (int) pos.x, (int) pos.y, (int) pos.z)));

            ParticleSystem<?> system = controller.systemsByIndex.get(systemId);
            Object data = ((ParticleSystemAccessor) system).getAdapter().deserializer().apply(buf);

            if (data != null) {
                FieldDataIsland island = new FieldDataIsland();
                island.targetObject(data, false);
                view.child(island.mainContainer());
            }

            return true;
        });

        DrawPacketHandler.EVENT.addPhaseOrdering(Event.DEFAULT_PHASE, LAST_PHASE);
        DrawPacketHandler.EVENT.register(LAST_PHASE, (packet, view) -> {
            if (NetworkUtil.getChannelOrNull(packet.packet()) != null) {
                view.child(Components.label(Text.translatable("text.gadget.packet_not_supported")));
                return true;
            }

            FieldDataIsland island = new FieldDataIsland();
            island.targetObject(packet.packet(), false);

            view.child(island.mainContainer());
            return true;
        });
    }
}
