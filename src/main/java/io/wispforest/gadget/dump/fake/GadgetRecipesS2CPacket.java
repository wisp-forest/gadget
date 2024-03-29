package io.wispforest.gadget.dump.fake;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.dump.fake.recipe.FakeGadgetRecipe;
import io.wispforest.gadget.dump.fake.recipe.ReadErrorRecipe;
import io.wispforest.gadget.dump.fake.recipe.WriteErrorRecipe;
import io.wispforest.gadget.util.NetworkUtil;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GadgetRecipesS2CPacket(List<RecipeEntry<?>> recipes) implements FakeGadgetPacket {
    public static final int ID = -4;

    public static GadgetRecipesS2CPacket read(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        int size = buf.readVarInt();
        List<RecipeEntry<?>> recipes = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            PacketByteBuf subBuf = NetworkUtil.readOfLengthIntoTmp(buf);

            try {
                recipes.add(SynchronizeRecipesS2CPacket.readRecipe(subBuf));
            } catch (Exception e) {
                subBuf.readerIndex(0);
                recipes.add(ReadErrorRecipe.from(e, subBuf));
            }
        }

        return new GadgetRecipesS2CPacket(recipes);
    }

    @Override
    public int id() {
        return ID;
    }

    @Override
    public Packet<?> unwrapVanilla() {
        return new SynchronizeRecipesS2CPacket(recipes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void writeToDump(PacketByteBuf buf, NetworkState state, NetworkSide side) {
        buf.writeVarInt(recipes.size());

        for (var recipe : recipes) {
            try (var ignored = NetworkUtil.writeByteLength(buf)) {
                int startWriteIdx = buf.writerIndex();

                try {
                    if (recipe.value() instanceof FakeGadgetRecipe fakeRecipe) {
                        writeFake(buf, new RecipeEntry<>(recipe.id(), fakeRecipe));
                        return;
                    }

                    RecipeSerializer<?> serializer = recipe.value().getSerializer();
                    Identifier serializerId = Registries.RECIPE_SERIALIZER.getKey(serializer).map(RegistryKey::getValue).orElse(null);

                    if (serializerId == null)
                        throw new UnsupportedOperationException(serializer + " is not a registered serializer!");

                    buf.writeIdentifier(serializerId);
                    buf.writeIdentifier(recipe.id());

                    ((RecipeSerializer<Recipe<?>>) serializer).write(buf, recipe.value());
                } catch (Exception e) {
                    buf.writerIndex(startWriteIdx);

                    Gadget.LOGGER.error("Error while writing recipe {}", recipe, e);

                    WriteErrorRecipe writeError = WriteErrorRecipe.from(e);
                    writeFake(buf, new RecipeEntry<>(recipe.id(), writeError));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void writeFake(PacketByteBuf buf, RecipeEntry<? extends FakeGadgetRecipe> recipe) {
        buf.writeIdentifier(recipe.value().getSerializer().id());
        buf.writeIdentifier(recipe.id());
        ((RecipeSerializer<FakeGadgetRecipe>) recipe.value().getSerializer()).write(buf, recipe.value());
    }
}
