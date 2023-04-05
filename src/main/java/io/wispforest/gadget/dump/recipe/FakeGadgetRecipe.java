package io.wispforest.gadget.dump.recipe;

import com.google.gson.JsonObject;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public interface FakeGadgetRecipe extends Recipe<Inventory> {
    @Override
    default boolean matches(Inventory inventory, World world) {
        throw new UnsupportedOperationException();
    }

    @Override
    default ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean fits(int width, int height) {
        throw new UnsupportedOperationException();
    }

    @Override
    default ItemStack getOutput(DynamicRegistryManager registryManager) {
        throw new UnsupportedOperationException();
    }

    @Override
    default DefaultedList<ItemStack> getRemainder(Inventory inventory) {
        throw new UnsupportedOperationException();
    }

    @Override
    default DefaultedList<Ingredient> getIngredients() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean isIgnoredInRecipeBook() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean showNotification() {
        throw new UnsupportedOperationException();
    }

    @Override
    default String getGroup() {
        throw new UnsupportedOperationException();
    }

    @Override
    default ItemStack createIcon() {
        throw new UnsupportedOperationException();
    }

    @Override
    default RecipeType<?> getType() {
        throw new UnsupportedOperationException();
    }

    @Override
    FakeSerializer<?> getSerializer();

    @Override
    default boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    interface FakeSerializer<R extends FakeGadgetRecipe> extends RecipeSerializer<R> {
        @Override
        default R read(Identifier id, JsonObject json) {
            throw new UnsupportedOperationException();
        }

        Identifier id();
    }
}
