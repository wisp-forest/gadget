package io.wispforest.gadget.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.pond.MixinState;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

@Mixin(NamespaceResourceManager.class)
public class NamespaceResourceManagerMixin {
    @WrapOperation(method = "findAndAdd", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePack;findResources(Lnet/minecraft/resource/ResourceType;Ljava/lang/String;Ljava/lang/String;Ljava/util/function/Predicate;)Ljava/util/Collection;"))
    private Collection<Identifier> ignoreErrorsIfNeeded(ResourcePack pack, ResourceType type, String namespace, String prefix, Predicate<Path> predicate, Operation<Collection<Identifier>> original) {
        if (MixinState.IS_IGNORING_ERRORS.get() != null) {
            try {
                return original.call(pack, type, namespace, prefix, predicate);
            } catch (Exception e) {
                Gadget.LOGGER.error("Resource pack {} threw an error while loading all resources, which has been ignored", pack.getName());
                return Collections.emptyList();
            }
        } else {
            return original.call(pack, type, namespace, prefix, predicate);
        }
    }
}
