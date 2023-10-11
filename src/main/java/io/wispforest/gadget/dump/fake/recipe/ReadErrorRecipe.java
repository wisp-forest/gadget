package io.wispforest.gadget.dump.fake.recipe;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;

import java.util.concurrent.ThreadLocalRandom;

public record ReadErrorRecipe(byte[] data, Exception exception) implements FakeGadgetRecipe {
    public static RecipeEntry<ReadErrorRecipe> from(Exception exception, PacketByteBuf buf) {
        int start = buf.readerIndex();
        Identifier recipeId = new Identifier(
            "gadget-fake",
            "cringe-recipe-bruh-" + ThreadLocalRandom.current().nextInt()
        );

        try {
            buf.readIdentifier();
            recipeId = buf.readIdentifier();
        } catch (Exception e) {
            exception.addSuppressed(e);
        }

        buf.readerIndex(start);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        return new RecipeEntry<>(recipeId, new ReadErrorRecipe(bytes, exception));
    }

    @Override
    public FakeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException();
    }
}
