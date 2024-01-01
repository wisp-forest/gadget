package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.dump.ClientPacketDumper;
import io.wispforest.gadget.client.dump.DumpPrimer;
import io.wispforest.gadget.client.gui.ContextMenuScreens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.UnknownHostException;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerEntryMixin {
    @Shadow @Final private MultiplayerScreen screen;

    @Shadow @Final private ServerInfo server;

    @Shadow @Final private MinecraftClient client;

    @Shadow public abstract void saveFile();

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onRightClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_RIGHT) return;
        if (!Gadget.CONFIG.rightClickDump()) return;

        ContextMenuScreens.contextMenuAt(screen, mouseX, mouseY)
            .button(Text.translatable("text.gadget.join_with_dump"), dropdown2 -> {
                DumpPrimer.isPrimed = true;

                    this.screen.select((MultiplayerServerListWidget.ServerEntry)(Object) this);
                    this.screen.connect();
                })
            .button(Text.translatable("text.gadget.query_with_dump"), dropdown2 -> {
                DumpPrimer.isPrimed = true;
                ClientPacketDumper.start(false);

                try {
                    this.screen.getServerListPinger().add(this.server, () -> this.client.execute(this::saveFile));
                } catch (UnknownHostException var2x) {
                    this.server.ping = -1L;
                    this.server.label = MultiplayerServerListWidgetAccessor.getCANNOT_RESOLVE_TEXT();
                } catch (Exception var3x) {
                    this.server.ping = -1L;
                    this.server.label = MultiplayerServerListWidgetAccessor.getCANNOT_CONNECT_TEXT();
                }
            });

        cir.setReturnValue(true);
    }
}