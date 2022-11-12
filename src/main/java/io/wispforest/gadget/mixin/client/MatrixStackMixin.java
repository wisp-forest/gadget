package io.wispforest.gadget.mixin.client;

import io.wispforest.gadget.client.MatrixStackLogger;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

@Mixin(MatrixStack.class)
public class MatrixStackMixin {
    @Shadow @Final private Deque<MatrixStack.Entry> stack;

    @Inject(method = "pop", at = @At("HEAD"), cancellable = true)
    private void onPop(CallbackInfo ci) {
        if (stack.size() == 1
         && MatrixStackLogger.tripError((MatrixStack) (Object) this, "Tried to pop empty MatrixStack")) {
            ci.cancel();
            return;
        }

        MatrixStackLogger.logOp((MatrixStack)(Object) this, false, stack.size() - 2);
    }

    @Inject(method = "push", at = @At("HEAD"))
    private void onPush(CallbackInfo ci) {
        MatrixStackLogger.logOp((MatrixStack)(Object) this, true, stack.size() - 1);
    }
}
