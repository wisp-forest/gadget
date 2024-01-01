package io.wispforest.gadget.mixin.client;

import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiplayerServerListWidget.class)
public interface MultiplayerServerListWidgetAccessor {

    @Accessor
    static Text getCANNOT_RESOLVE_TEXT() {
        throw new AssertionError();
    }

    @Accessor
    static Text getCANNOT_CONNECT_TEXT() {
        throw new AssertionError();
    }

}
