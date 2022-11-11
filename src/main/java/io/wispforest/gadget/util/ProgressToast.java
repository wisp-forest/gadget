package io.wispforest.gadget.util;

import io.wispforest.gadget.client.gui.ProgressToastImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public interface ProgressToast {
    static ProgressToast create(Text headText) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return new ProgressToastImpl(headText);
        } else {
            return new Dummy();
        }
    }

    void step(Text text);

    void followProgress(ProgressInputStream stream, int total);

    default InputStream loadWithProgress(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
        int total = connection.getContentLength();

        if (total == -1) {
            return is;
        } else {
            ProgressInputStream progress = new ProgressInputStream(is);
            followProgress(progress, total);
            return progress;
        }
    }

    void finish();

    class Dummy implements ProgressToast {
        @Override
        public void step(Text text) {

        }

        @Override
        public void followProgress(ProgressInputStream stream, int total) {

        }

        @Override
        public void finish() {

        }
    }
}
