package io.wispforest.gadget.mixin.owo;

import io.wispforest.owo.network.OwoNetChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = OwoNetChannel.class, remap = false)
public interface OwoNetChannelAccessor {
    @Accessor("REGISTERED_CHANNELS")
    static Map<Identifier, OwoNetChannel> getRegisteredChannels() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    Int2ObjectMap<IndexedSerializerAccessor> getSerializersByIndex();
}
