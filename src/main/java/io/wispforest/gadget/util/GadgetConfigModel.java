package io.wispforest.gadget.util;

import io.wispforest.gadget.mappings.*;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Hook;
import io.wispforest.owo.config.annotation.Modmenu;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

@Modmenu(modId = "gadget")
@Config(name = "gadget", wrapperName = "GadgetConfig")
public class GadgetConfigModel {
    public boolean menuButtonEnabled = true;
    public boolean rightClickDump = true;
    public boolean noFrequentPackets = true;
    public boolean debugKeysInScreens = true;
    @Hook public MappingsType mappings = FabricLoader.getInstance().isDevelopmentEnvironment() ? MappingsType.LOCAL : MappingsType.YARN;

    public enum MappingsType {
        // TODO: finish everything™
        LOCAL(() -> LocalMappings.INSTANCE),
        YARN(YarnMappings::new),
        MOJANG(MojangMappings::new),
        QUILT(QuiltMappings::new);

        private final Supplier<Mappings> factory;

        MappingsType(Supplier<Mappings> factory) {
            this.factory = factory;
        }

        public Supplier<Mappings> factory() {
            return factory;
        }
    }
}
