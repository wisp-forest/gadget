package io.wispforest.gadget.mixin;

import net.minecraft.resource.ZipResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ZipResourcePack.class)
public class ZipResourcePackMixin {
    @ModifyVariable(method = "findResources", at = @At(value = "INVOKE", target = "Ljava/util/Enumeration;hasMoreElements()Z"), ordinal = 3)
    private String makeItBetter(String in) {
        return in.replace("//", "/");
    }
}
