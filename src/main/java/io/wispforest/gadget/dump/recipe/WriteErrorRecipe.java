package io.wispforest.gadget.dump.recipe;

import io.wispforest.gadget.Gadget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

public record WriteErrorRecipe(Identifier id, String exceptionText) implements FakeGadgetRecipe {
    public static WriteErrorRecipe from(Identifier id, Exception e) {
        CharArrayWriter writer = new CharArrayWriter();
        e.printStackTrace(new PrintWriter(writer));
        String fullExceptionText = writer.toString();

        return new WriteErrorRecipe(id, fullExceptionText);
    }

    @Override
    public Identifier getId() {
        return id;
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
        public WriteErrorRecipe read(Identifier id, PacketByteBuf buf) {
            return new WriteErrorRecipe(id, buf.readString());
        }

        @Override
        public void write(PacketByteBuf buf, WriteErrorRecipe recipe) {
            buf.writeString(recipe.exceptionText);
        }
    }
}
