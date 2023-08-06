package io.wispforest.gadget.mixin.nbt;

import com.google.common.collect.ForwardingMap;
import io.wispforest.gadget.nbt.LockableNbt;
import io.wispforest.gadget.nbt.LockableNbtInternal;
import io.wispforest.gadget.nbt.NbtLock;
import io.wispforest.gadget.nbt.NbtLocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public class ItemStackMixin implements LockableNbtInternal {
    @Shadow private @Nullable NbtCompound nbt;
    @Unique private final List<NbtLock> gadget$locks = new ArrayList<>();

    @Override
    public List<NbtLock> gadget$locks() {
        return gadget$locks;
    }

    @Override
    public void lock(NbtLock lock) {
        gadget$locks().add(lock);

        if (nbt instanceof LockableNbt lockable) {
            lockable.lock(lock);
        }
    }

    @Override
    public void unlock(NbtLock lock) {
        gadget$locks().remove(lock);

        if (nbt instanceof LockableNbt lockable) {
            lockable.unlock(lock);
        }
    }

    @Inject(method = "setNbt", at = @At("HEAD"))
    private void checkMutability(NbtCompound nbt, CallbackInfo ci) {
        gadget$checkWrite();
    }

    @Inject(method = {"removeCustomName", "removeSubNbt"}, at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/item/ItemStack;nbt:Lnet/minecraft/nbt/NbtCompound;"))
    private void checkMutability(CallbackInfo ci) {
        gadget$checkWrite();
    }

    @Inject(method = "getName", at = @At("HEAD"))
    private void lockGetName(CallbackInfoReturnable<Text> cir) {
        lock(NbtLocks.GET_NAME);
    }

    @Inject(method = "getName", at = @At("RETURN"))
    private void unlockGetName(CallbackInfoReturnable<Text> cir) {
        unlock(NbtLocks.GET_NAME);
    }
}
