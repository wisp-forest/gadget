package io.wispforest.gadget.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.util.PathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PathUtil.class)
public class PathUtilMixin {
    @Inject(method = "split", at = @At("HEAD"), cancellable = true)
    private static void mald(String path, CallbackInfoReturnable<DataResult<List<String>>> cir) {
        if (path.equals(""))
            cir.setReturnValue(DataResult.success(List.of()));
    }
}
