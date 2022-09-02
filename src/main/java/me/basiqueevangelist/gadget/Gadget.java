package me.basiqueevangelist.gadget;

import me.basiqueevangelist.gadget.network.GadgetNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class Gadget implements ModInitializer {
    public static final String MODID = "gadget";

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        GadgetNetworking.init();
    }
}
