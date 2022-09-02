package me.basiqueevangelist.gadget.client;

import me.basiqueevangelist.gadget.client.gui.BlockEntityDataScreen;
import me.basiqueevangelist.gadget.client.gui.EntityDataScreen;
import me.basiqueevangelist.gadget.network.*;
import me.basiqueevangelist.gadget.util.FieldPath;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class GadgetClient implements ClientModInitializer {
    public static KeyBinding INSPECT_KEY = new KeyBinding("key.gadget.inspect", GLFW.GLFW_KEY_I, KeyBinding.MISC_CATEGORY);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(INSPECT_KEY);

        GadgetNetworking.CHANNEL.registerClientbound(BlockEntityDataS2CPacket.class, (packet, access) -> {
            if (access.runtime().currentScreen instanceof BlockEntityDataScreen gui) {
                gui.applyData(packet);
                return;
            }

            access.runtime().setScreen(new BlockEntityDataScreen(packet));
        });

        GadgetNetworking.CHANNEL.registerClientbound(EntityDataS2CPacket.class, (packet, access) -> {
            if (access.runtime().currentScreen instanceof EntityDataScreen gui) {
                gui.applyData(packet);
                return;
            }

            access.runtime().setScreen(new EntityDataScreen(packet));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!INSPECT_KEY.wasPressed()) return;

            if (!GadgetNetworking.CHANNEL.canSendToServer()) {
                if (client.player != null)
                    client.player.sendMessage(Text.translatable("message.gadget.fail.noserversupport").formatted(Formatting.RED), true);

                return;
            }

            HitResult target = client.crosshairTarget;

            if (target == null) return;

            if (target instanceof EntityHitResult ehr) {
                GadgetNetworking.CHANNEL.clientHandle().send(new RequestEntityDataC2SPacket(ehr.getEntity().getId(), new FieldPath(List.of())));
            } else {
                BlockPos blockPos = target instanceof BlockHitResult blockHitResult ? blockHitResult.getBlockPos() : new BlockPos(target.getPos());

                GadgetNetworking.CHANNEL.clientHandle().send(new RequestBlockEntityDataC2SPacket(blockPos, new FieldPath(List.of())));
            }
        });
    }

}
