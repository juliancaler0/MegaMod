package com.ultra.megamod.lib.emf.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.ultra.megamod.lib.emf.EMF;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON-backed config handler for the EMF port. Follows the exact same shape as
 * {@code ETFConfigHandler} for consistency.
 * <p>
 * File path: {@code <gamedir>/config/megamod-emf.json}. GSON pretty-printed so
 * hand editing is fine.
 */
public class EMFConfigHandler {

    private static final String FILE_NAME = "megamod-emf.json";
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private EMFConfig config;
    private final Path file;

    public EMFConfigHandler() {
        Path configDir;
        try {
            configDir = FMLPaths.CONFIGDIR.get();
        } catch (Throwable t) {
            configDir = Path.of("config");
        }
        this.file = configDir.resolve(FILE_NAME);
        this.config = loadFromDisk();
        // Mirror a couple of fields that live as static flags on EMF.java
        EMF.logModelCreationData = config.logModelCreationData;
        EMF.enforceOptiFineAnimSyntaxLimits = config.enforceOptiFineAnimSyntaxLimits;
    }

    public EMFConfig getConfig() {
        return config;
    }

    public void setConfig(EMFConfig newConfig) {
        if (newConfig != null) {
            this.config = newConfig;
            EMF.logModelCreationData = config.logModelCreationData;
            EMF.enforceOptiFineAnimSyntaxLimits = config.enforceOptiFineAnimSyntaxLimits;
        }
    }

    public void saveToFile() {
        try {
            Path parent = file.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            String json = GSON.toJson(config);
            Files.writeString(file, json);
            // Re-mirror the live flags to pick up edits from the config screen.
            EMF.logModelCreationData = config.logModelCreationData;
            EMF.enforceOptiFineAnimSyntaxLimits = config.enforceOptiFineAnimSyntaxLimits;
        } catch (IOException e) {
            EMF.LOGGER.warn("[EMF] Failed to save config to {}: {}", file, e.toString());
        }
    }

    public void loadFromFile() {
        this.config = loadFromDisk();
        EMF.logModelCreationData = config.logModelCreationData;
        EMF.enforceOptiFineAnimSyntaxLimits = config.enforceOptiFineAnimSyntaxLimits;
    }

    private EMFConfig loadFromDisk() {
        if (!Files.exists(file)) {
            EMFConfig fresh = new EMFConfig();
            try {
                Path parent = file.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.writeString(file, GSON.toJson(fresh));
            } catch (IOException ignored) {
            }
            return fresh;
        }
        try {
            String text = Files.readString(file);
            EMFConfig loaded = GSON.fromJson(text, EMFConfig.class);
            return loaded != null ? loaded : new EMFConfig();
        } catch (IOException | JsonSyntaxException e) {
            EMF.LOGGER.warn("[EMF] Failed to load config from {}, using defaults: {}", file, e.toString());
            return new EMFConfig();
        }
    }
}
