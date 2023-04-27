package io.wispforest.gadget.util;

import com.google.common.io.CountingInputStream;
import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.client.gui.ProgressToastImpl;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongSupplier;

@SuppressWarnings("UnstableApiUsage")
public interface ProgressToast {
    static ProgressToast create(Text headText) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            return new ProgressToastImpl(headText);
        } else {
            return dummy();
        }
    }

    static ProgressToast dummy() {
        return new Dummy();
    }

    void step(Text text);

    void followProgress(LongSupplier stream, long total);

    default InputStream loadWithProgress(Path path) throws IOException {
        long size = Files.size(path);
        var bis = new BufferedInputStream(Files.newInputStream(path));

        if (size == 0) {
            return bis;
        } else {
            CountingInputStream progress = new CountingInputStream(bis);
            followProgress(progress::getCount, size);
            return progress;
        }
    }

    default InputStream loadWithProgress(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        BufferedInputStream is = new BufferedInputStream(connection.getInputStream());
        int total = connection.getContentLength();

        if (total == -1) {
            return is;
        } else {
            CountingInputStream progress = new CountingInputStream(is);
            followProgress(progress::getCount, total);
            return progress;
        }
    }

    void force();

    void finish(Text text, boolean hideImmediately);

    void oom(OutOfMemoryError oom);

    default CompletableFuture<Void> follow(CompletableFuture<Void> future, boolean closeImmediately) {
        return future.whenComplete((res, e) -> {
            if (e != null) {
                if (e instanceof OutOfMemoryError oom) {
                    // Welp.

                    oom(oom);
                    return;
                }

                Gadget.LOGGER.error("Loading failed with exception", e);
                force();
                finish(Text.translatable("message.gadget.progress.failed"), false);
            } else {
                finish(Text.translatable("message.gadget.progress.finished"), closeImmediately);
            }
        });
    }

    class Dummy implements ProgressToast {
        @Override
        public void step(Text text) {

        }

        @Override
        public void followProgress(LongSupplier stream, long total) {

        }

        @Override
        public void force() {

        }

        @Override
        public void finish(Text text, boolean hideImmediately) {

        }

        @Override
        public void oom(OutOfMemoryError oom) {

        }
    }
}
