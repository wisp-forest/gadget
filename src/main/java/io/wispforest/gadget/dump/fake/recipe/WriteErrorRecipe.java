package io.wispforest.gadget.dump.fake.recipe;

import com.mojang.serialization.Codec;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.util.ThrowableUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record WriteErrorRecipe(Identifier id, String exceptionText) implements FakeGadgetRecipe {
    public static WriteErrorRecipe from(Identifier id, Exception e) {
        return new WriteErrorRecipe(id, ThrowableUtil.throwableToString(e));
    }

    @Override
    public FakeSerializer<WriteErrorRecipe> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements FakeSerializer<WriteErrorRecipe> {
        public static final Identifier ID = Gadget.id("write_error");
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Identifier id() {
            return ID;
        }

        @Override
        public void write(PacketByteBuf buf, WriteErrorRecipe recipe) {
            buf.writeString(recipe.exceptionText);
        }

        @Override
        public Codec<WriteErrorRecipe> codec() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'codec'");
        }

        @Override
        public WriteErrorRecipe read(PacketByteBuf buf) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'read'");
        }
    }
}
