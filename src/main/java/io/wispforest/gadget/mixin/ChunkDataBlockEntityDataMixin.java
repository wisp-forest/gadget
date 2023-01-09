package io.wispforest.gadget.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.collection.IndexedIterable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/network/packet/s2c/play/ChunkData$BlockEntityData")
public class ChunkDataBlockEntityDataMixin {
    private int gadget$originalBlockEntityTypeId = -1;

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;)Ljava/lang/Object;"))
    private void saveId(PacketByteBuf buf, CallbackInfo ci) {
        int readerIdx = buf.readerIndex();

        try {
            gadget$originalBlockEntityTypeId = buf.readVarInt();
        } finally {
            buf.readerIndex(readerIdx);
        }
    }

    @WrapWithCondition(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;writeRegistryValue(Lnet/minecraft/util/collection/IndexedIterable;Ljava/lang/Object;)V"))
    private boolean useId(PacketByteBuf buf, IndexedIterable<BlockEntityType<?>> registry, Object type) {
        if (type == null) {
            buf.writeVarInt(gadget$originalBlockEntityTypeId);
            return false;
        } else {
            return true;
        }
    }

}
