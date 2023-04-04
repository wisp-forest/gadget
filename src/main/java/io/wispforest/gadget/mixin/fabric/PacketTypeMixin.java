package io.wispforest.gadget.mixin.fabric;

import io.wispforest.gadget.network.FabricPacketHacks;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(PacketType.class)
public class PacketTypeMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void stowPacketType(Identifier id, Function<?, ?> constructor, CallbackInfo ci) {
        FabricPacketHacks.saveType((PacketType<?>)(Object) this);
    }
}
