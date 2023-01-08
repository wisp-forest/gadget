package io.wispforest.gadget.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.command.ConfigCommand;
import io.wispforest.gadget.client.command.ReloadMappingsCommand;
import io.wispforest.gadget.client.config.GadgetConfigScreen;
import io.wispforest.gadget.client.gui.inspector.UIInspector;
import io.wispforest.gadget.client.nbt.StackNbtDataScreen;
import io.wispforest.gadget.client.resource.ViewResourcesScreen;
import io.wispforest.gadget.mappings.MappingsManager;
import io.wispforest.gadget.mixin.client.HandledScreenAccessor;
import io.wispforest.gadget.network.*;
import io.wispforest.gadget.client.dump.handler.PacketHandlers;
import io.wispforest.gadget.client.dump.PacketDumper;
import io.wispforest.gadget.client.field.FieldDataScreen;
import io.wispforest.gadget.client.gui.GadgetScreen;
import io.wispforest.gadget.network.packet.c2s.RequestDataC2SPacket;
import io.wispforest.gadget.network.packet.c2s.RequestResourceC2SPacket;
import io.wispforest.gadget.network.packet.s2c.DataS2CPacket;
import io.wispforest.gadget.network.packet.s2c.ResourceDataS2CPacket;
import io.wispforest.gadget.network.packet.s2c.ResourceListS2CPacket;
import io.wispforest.gadget.path.ObjectPath;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.layers.Layer;
import io.wispforest.owo.ui.layers.Layers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import java.io.ByteArrayInputStream;
import java.util.List;

public class GadgetClient implements ClientModInitializer {
    public static KeyBinding INSPECT_KEY = new KeyBinding("key.gadget.inspect", GLFW.GLFW_KEY_I, KeyBinding.MISC_CATEGORY);
    public static KeyBinding DUMP_KEY = new KeyBinding("key.gadget.dump", GLFW.GLFW_KEY_K, KeyBinding.MISC_CATEGORY);

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(INSPECT_KEY);
        KeyBindingHelper.registerKeyBinding(DUMP_KEY);

        PacketHandlers.init();
        UIInspector.init();
        ServerData.init();

        ConfigScreen.registerProvider("gadget", GadgetConfigScreen::new);

        GadgetNetworking.CHANNEL.registerClientbound(DataS2CPacket.class, (packet, access) -> {
            if (access.runtime().currentScreen instanceof FieldDataScreen gui && !gui.isClient() && gui.target().equals(packet.target())) {
                gui.addFieldData(packet.fields());
                return;
            }

            var screen = new FieldDataScreen(packet.target(), false);
            screen.addFieldData(packet.fields());
            access.runtime().setScreen(screen);
        });

        GadgetNetworking.CHANNEL.registerClientbound(ResourceListS2CPacket.class, (packet, access) -> {
            var screen = new ViewResourcesScreen(access.runtime().currentScreen, packet.resources());

            screen.resRequester(
                (id, idx) -> GadgetNetworking.CHANNEL.clientHandle().send(new RequestResourceC2SPacket(id, idx)));

            access.runtime().setScreen(screen);
        });

        GadgetNetworking.CHANNEL.registerClientbound(ResourceDataS2CPacket.class, (packet, access) -> {
            if (!(access.runtime().currentScreen instanceof ViewResourcesScreen screen))
                return;

            screen.openFile(packet.id(), () -> new ByteArrayInputStream(packet.data()));
        });

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.getOverlay() == null) {
                MappingsManager.init();
            }
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
                return;
            }

            Entity camera = client.getCameraEntity();
            if (camera == null) camera = client.player;

            HitResult target = raycast(camera, client.getTickDelta());

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

        List<String> alignToButtons = List.of(
            "menu.multiplayer",
            "menu.shareToLan",
            "menu.playerReporting"
        );

        Layers.add(Containers::verticalFlow, instance -> {
            if (!Gadget.CONFIG.menuButtonEnabled()) return;

            instance.adapter.rootComponent.child(
                Components.button(
                    Text.translatable("text.gadget.menu_button"),
                    button -> MinecraftClient.getInstance().setScreen(new GadgetScreen(instance.screen))
                ).<ButtonWidget>configure(button -> {
                    button.margins(Insets.left(4)).sizing(Sizing.fixed(20));
                    instance.alignComponentToWidget(widget -> {
                        if (!(widget instanceof ButtonWidget daButton)) return false;
                        return daButton.getMessage().getContent() instanceof TranslatableTextContent translatable
                            && alignToButtons.contains(translatable.getKey());
                    }, Layer.Instance.AnchorSide.RIGHT, 0, button);
                })
            );
        }, TitleScreen.class, GameMenuScreen.class);

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof HandledScreen<?> handled)
                ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                    if (!INSPECT_KEY.matchesKey(key, scancode)) return true;

                    double mouseX = client.mouse.getX()
                        * (double)client.getWindow().getScaledWidth() / (double)client.getWindow().getWidth();
                    double mouseY = client.mouse.getY()
                        * (double)client.getWindow().getScaledHeight() / (double)client.getWindow().getHeight();
                    var slot = ((HandledScreenAccessor) handled).callGetSlotAt(mouseX, mouseY);

                    if (slot == null) return true;
                    if (slot instanceof CreativeInventoryScreen.LockableSlot) return true;
                    if (slot.getStack().isEmpty()) return true;

                    client.setScreen(new StackNbtDataScreen(handled, slot));

                    return false;
                });

            ScreenKeyboardEvents.allowKeyPress(screen).register((screen1, key, scancode, modifiers) -> {
                if (!Screen.hasShiftDown()) return true;
                if (!INSPECT_KEY.matchesKey(key, scancode)) return true;

                UIInspector.dumpWidgetTree(screen1);

                return false;
            });
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            ReloadMappingsCommand.register(dispatcher);
            ConfigCommand.register(dispatcher);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (Gadget.CONFIG.internalSettings.injectMatrixStackErrors() && Screen.hasShiftDown()) {
                context.matrixStack().pop();
            }
        });

        for (EntrypointContainer<GadgetClientEntrypoint> container : FabricLoader.getInstance().getEntrypointContainers("gadget:client_init", GadgetClientEntrypoint.class)) {
            try {
                container.getEntrypoint().onGadgetClientInit();
            } catch (Exception e) {
                Gadget.LOGGER.error("{}'s `gadget:client_init` entrypoint handler threw an exception",
                    container.getProvider().getMetadata().getId(), e);
            }
        }
    }

    // 100% not stolen from owo-whats-this
    // https://github.com/wisp-forest/owo-whats-this/blob/master/src/main/java/io/wispforest/owowhatsthis/OwoWhatsThis.java#L155-L171.
    public static HitResult raycast(Entity entity, float tickDelta) {
        var blockTarget = entity.raycast(5, tickDelta, false);

        var maxReach = entity.getRotationVec(tickDelta).multiply(5);
        var entityTarget = ProjectileUtil.raycast(
            entity,
            entity.getEyePos(),
            entity.getEyePos().add(maxReach),
            entity.getBoundingBox().stretch(maxReach),
            candidate -> true,
            5 * 5
        );

        return entityTarget != null && entityTarget.squaredDistanceTo(entity) < blockTarget.squaredDistanceTo(entity)
            ? entityTarget
            : blockTarget;
    }
}
