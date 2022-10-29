package io.wispforest.gadget.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public final class DownloadUtil {
    private static final Gson GSON = new Gson();

    private DownloadUtil() {

    }

    public static <T> T read(String url, Class<T> klass) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        try (var is = conn.getInputStream()) {
            return GSON.fromJson(new InputStreamReader(is), klass);
        }
    }

    public static JsonObject read(String url) throws IOException {
        URLConnection conn = new URL(url).openConnection();
        try (var is = conn.getInputStream()) {
            return JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
        }
    }
}
