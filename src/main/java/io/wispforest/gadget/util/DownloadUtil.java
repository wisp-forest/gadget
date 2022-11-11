package io.wispforest.gadget.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public final class DownloadUtil {
    private static final Gson GSON = new Gson();

    private DownloadUtil() {

    }

    public static <T> T read(ProgressToast toast, String url, Class<T> klass) throws IOException {
        try (var is = toast.loadWithProgress(new URL(url))) {
            return GSON.fromJson(new InputStreamReader(new BufferedInputStream(is)), klass);
        }
    }

    public static JsonObject read(ProgressToast toast, String url) throws IOException {
        try (var is = toast.loadWithProgress(new URL(url))) {
            return JsonParser.parseReader(new InputStreamReader(new BufferedInputStream(is))).getAsJsonObject();
        }
    }
}
