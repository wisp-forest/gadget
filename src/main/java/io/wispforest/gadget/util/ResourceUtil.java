package io.wispforest.gadget.util;

import io.wispforest.gadget.pond.MixinState;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class ResourceUtil {
    private ResourceUtil() {

    }

    public static Map<Identifier, List<Resource>> collectAllResources(ResourceManager manager) {
        try {
            MixinState.IS_IGNORING_ERRORS.set(true);
            return manager.findAllResources("", x -> true);
        } finally {
            MixinState.IS_IGNORING_ERRORS.remove();
        }
    }
}
