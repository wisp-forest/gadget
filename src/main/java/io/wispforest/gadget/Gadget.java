package io.wispforest.gadget;

import io.wispforest.gadget.network.GadgetNetworking;
import me.basiqueevangelist.gadget.util.GadgetConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Gadget implements ModInitializer {
    public static final String MODID = "gadget";
    public static final GadgetConfig CONFIG = GadgetConfig.createAndLoad();
    public static final Logger LOGGER = LoggerFactory.getLogger("gadget");

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    @Override
    public void onInitialize() {
        GadgetNetworking.init();

        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.warn("""

                ░██╗░░░░░░░██╗░█████╗░██████╗░███╗░░██╗██╗███╗░░██╗░██████╗░
                ░██║░░██╗░░██║██╔══██╗██╔══██╗████╗░██║██║████╗░██║██╔════╝░
                ░╚██╗████╗██╔╝███████║██████╔╝██╔██╗██║██║██╔██╗██║██║░░██╗░
                ░░████╔═████║░██╔══██║██╔══██╗██║╚████║██║██║╚████║██║░░╚██╗
                ░░╚██╔╝░╚██╔╝░██║░░██║██║░░██║██║░╚███║██║██║░╚███║╚██████╔╝
                ░░░╚═╝░░░╚═╝░░╚═╝░░╚═╝╚═╝░░╚═╝╚═╝░░╚══╝╚═╝╚═╝░░╚══╝░╚═════╝░
                 Gadget doesn't work very well in production. Caveat emptor.""");
        }
    }
}
