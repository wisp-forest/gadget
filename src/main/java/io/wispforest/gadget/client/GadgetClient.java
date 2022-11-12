package io.wispforest.gadget.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.command.ReloadMappingsCommand;
import io.wispforest.gadget.client.nbt.StackNbtDataScreen;
import io.wispforest.gadget.mixin.client.HandledScreenAccessor;
import io.wispforest.gadget.network.*;
import io.wispforest.gadget.client.dump.handler.DrawPacketHandlers;
import io.wispforest.gadget.client.dump.PacketDumper;
import io.wispforest.gadget.client.field.FieldDataScreen;
import io.wispforest.gadget.client.gui.GadgetScreen;
import io.wispforest.gadget.client.gui.VanillaInspector;
import io.wispforest.gadget.network.packet.c2s.RequestDataC2SPacket;
import io.wispforest.gadget.network.packet.s2c.DataS2CPacket;
import io.wispforest.gadget.path.ObjectPath;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class GadgetClient implements ClientModInitializer {
    public static KeyBinding INSPECT_KEY = new KeyBinding("key.gadget.inspect", GLFW.GLFW_KEY_I, KeyBinding.MISC_CATEGORY);
    public static KeyBinding DUMP_KEY = new KeyBinding("key.gadget.dump", GLFW.GLFW_KEY_K, KeyBinding.MISC_CATEGORY);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(INSPECT_KEY);
        KeyBindingHelper.registerKeyBinding(DUMP_KEY);

        DrawPacketHandlers.init();
        VanillaInspector.init();
        ServerData.init();

        GadgetNetworking.CHANNEL.registerClientbound(DataS2CPacket.class, (packet, access) -> {
            if (access.runtime().currentScreen instanceof FieldDataScreen gui && !gui.isClient() && gui.target().equals(packet.target())) {
                packet.fields().forEach(gui::addFieldData);
                return;
            }

            var screen = new FieldDataScreen(packet.target(), false);
            packet.fields().forEach(screen::addFieldData);
            access.runtime().setScreen(screen);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!INSPECT_KEY.wasPressed()) return;

            if (!GadgetNetworking.CHANNEL.canSendToServer()) {
                if (client.player != null)
                    client.player.sendMessage(Text.translatable("message.gadget.fail.noserversupport").formatted(Formatting.RED), true);

                return;
            }

            var perspective = client.options.getPerspective();

            if (!perspective.isFirstPerson() && client.player != null) {
                GadgetNetworking.CHANNEL.clientHandle().send(new RequestDataC2SPacket(new EntityTarget(client.player.getId()), ObjectPath.EMPTY));
            }

            HitResult target = client.crosshairTarget;

            if (target == null) return;

            if (target instanceof EntityHitResult ehr) {
                GadgetNetworking.CHANNEL.clientHandle().send(new RequestDataC2SPacket(new EntityTarget(ehr.getEntity().getId()), ObjectPath.EMPTY));
            } else {
                BlockPos blockPos = target instanceof BlockHitResult blockHitResult ? blockHitResult.getBlockPos() : new BlockPos(target.getPos());

                GadgetNetworking.CHANNEL.clientHandle().send(new RequestDataC2SPacket(new BlockEntityTarget(blockPos), ObjectPath.EMPTY));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!DUMP_KEY.wasPressed()) return;

            if (PacketDumper.isDumping()) {
                PacketDumper.stop();
            } else {
                PacketDumper.start(true);
            }
        });

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (Gadget.CONFIG.menuButtonEnabled()) {
                if (screen instanceof TitleScreen) {
                    int l = scaledHeight / 4 + 48;

                    Screens.getButtons(screen).add(new ButtonWidget(
                        scaledWidth / 2 + 104,
                        l + 48,
                        20,
                        20,
                        Text.translatable("text.gadget.menu_button"),
                        button -> client.setScreen(new GadgetScreen(screen))));
                } else if (screen instanceof GameMenuScreen) {
                    Screens.getButtons(screen).add(new ButtonWidget(
                        scaledWidth / 2 + 4 + 96 + 5,
                        scaledHeight / 4 + 96 - 16,
                        20,
                        20,
                        Text.translatable("text.gadget.menu_button"),
                        button -> client.setScreen(new GadgetScreen(screen))));
                }
            }

            if (screen instanceof HandledScreen<?> handled)
                ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (!INSPECT_KEY.matchesKey(key, scancode)) return true;

                    double mouseX = client.mouse.getX()
                        * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
                    double mouseY = client.mouse.getY()
                        * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
                    var slot = ((HandledScreenAccessor) handled).callGetSlotAt(mouseX, mouseY);

                    if (slot == null) return true;

                    ItemStack stack = slot.getStack();

                    if (!stack.hasNbt()) return true;

                    client.setScreen(new StackNbtDataScreen(handled, slot));

                    return false;
                });
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ReloadMappingsCommand.register(dispatcher);
        });

        FabricLoader.getInstance().getEntrypoints("gadget:client_init", GadgetClientEntrypoint.class)
            .forEach(GadgetClientEntrypoint::onGadgetClientInit);
    }
}
