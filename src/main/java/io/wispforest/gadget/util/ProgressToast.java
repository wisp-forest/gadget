package io.wispforest.gadget.util;

import io.wispforest.gadget.client.gui.ProgressToastImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

public interface ProgressToast {
    static ProgressToast create(Text headText) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return new ProgressToastImpl(headText);
        } else {
            return new Dummy();
        }
    }

    void step(Text text);

    void finish();

    class Dummy implements ProgressToast {
        @Override
        public void step(Text text) {

        }

        @Override
        public void finish() {

        }
    }
}
