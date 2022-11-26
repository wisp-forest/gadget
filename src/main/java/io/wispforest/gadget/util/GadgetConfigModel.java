package io.wispforest.gadget.util;

import io.wispforest.gadget.decompile.QuiltflowerVersions;
import io.wispforest.gadget.mappings.*;
import io.wispforest.owo.config.annotation.*;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Supplier;

@Config(name = "gadget", wrapperName = "GadgetConfig")
public class GadgetConfigModel {
    public boolean menuButtonEnabled = true;
    public boolean rightClickDump = true;
    public boolean dropChunkData = false;
    public boolean debugKeysInScreens = true;
    public boolean matrixStackDebugging = true;
    public boolean uiInspector = true;
    @RestartRequired public boolean silenceStartupErrors = true;
    @PredicateConstraint("isQuiltflowerVersionValid") public String quiltflowerVersion = "LATEST";
    @Hook public MappingsType mappings = FabricLoader.getInstance().isDevelopmentEnvironment() ? MappingsType.LOCAL : MappingsType.YARN;
    @Nest public InternalSettings internalSettings = new InternalSettings();

    public static boolean isQuiltflowerVersionValid(String version) {
        return version.equals("LATEST") || QuiltflowerVersions.versions().contains(version);
    }

    public static class InternalSettings {
        public boolean debugMatrixStackDebugging = false;
        public boolean injectMatrixStackErrors = false;
        public boolean inspectClasses = false;
    }

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
