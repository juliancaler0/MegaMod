package com.ultra.megamod.lib.etf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ultra.megamod.lib.etf.ETF;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON-backed ETF config handler. Loads on first access, persists on demand.
 * <p>
 * File path: {@code <gamedir>/config/megamod-etf.json}. JSON written with GSON using a
 * 2-space indent so the user can hand-edit it.
 */
public class ETFConfigHandler {

    private static final String FILE_NAME = "megamod-etf.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private ETFConfig config;
    private final Path file;

    public ETFConfigHandler() {
        Path configDir;
        try {
            configDir = FMLPaths.CONFIGDIR.get();
        } catch (Throwable t) {
            configDir = Path.of("config");
        }
        this.file = configDir.resolve(FILE_NAME);
        this.config = loadFromDisk();
    }

    public ETFConfig getConfig() {
        return config;
    }

    public void setConfig(ETFConfig newConfig) {
        if (newConfig != null) this.config = newConfig;
    }

    public ETFConfig copyOfConfig() {
        // Cheap deep copy via GSON round-trip
        String json = GSON.toJson(config);
        return GSON.fromJson(json, ETFConfig.class);
    }

    public void saveToFile() {
        try {
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            String json = GSON.toJson(config);
            Files.writeString(file, json);
        } catch (IOException e) {
            ETF.LOGGER.warn("[ETF] Failed to save config to {}: {}", file, e.toString());
        }
    }

    private ETFConfig loadFromDisk() {
        if (!Files.exists(file)) {
            ETFConfig fresh = new ETFConfig();
            // Attempt to write defaults so user has a reference file.
            try {
                Path parent = file.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.writeString(file, GSON.toJson(fresh));
            } catch (IOException ignored) {}
            return fresh;
        }
        try {
            String text = Files.readString(file);
            ETFConfig loaded = GSON.fromJson(text, ETFConfig.class);
            return loaded != null ? loaded : new ETFConfig();
        } catch (IOException | JsonSyntaxException e) {
            ETF.LOGGER.warn("[ETF] Failed to load config from {}, using defaults: {}", file, e.toString());
            return new ETFConfig();
        }
    }
}
