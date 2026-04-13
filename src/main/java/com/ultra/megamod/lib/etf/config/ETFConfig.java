package com.ultra.megamod.lib.etf.config;

import com.ultra.megamod.lib.etf.features.property_reading.properties.RandomProperties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Full ETF config object, ported from upstream {@code ETFConfig} (Phase C).
 * <p>
 * The upstream version extends {@code TConfig} and wires GUI entries in-line; we strip
 * the GUI wiring here (it lives in {@code ETFConfigScreen}) and keep the fields as
 * plain public state. Loaded/saved as JSON via {@link ETFConfigHandler}.
 */
@SuppressWarnings({"CanBeFinal", "unused"})
public class ETFConfig {

    // === Custom textures (random) ===
    public boolean enableCustomTextures = true;
    public boolean enableCustomBlockEntities = true;
    public UpdateFrequency textureUpdateFrequency_V2 = UpdateFrequency.Fast;
    public boolean disableVanillaDirectoryVariantTextures = false;

    // === Emissive / Enchant ===
    public boolean enableEmissiveTextures = true;
    public boolean enableEnchantedTextures = true;
    public boolean enableEmissiveBlockEntities = true;
    public EmissiveRenderModes emissiveRenderMode = EmissiveRenderModes.DULL;
    public boolean alwaysCheckVanillaEmissiveSuffix = true;
    public boolean enableArmorAndTrims = true;

    // === Skin features ===
    public boolean skinFeaturesEnabled = true;
    public SkinTransparencyMode skinTransparencyMode = SkinTransparencyMode.ETF_SKINS_ONLY;
    public boolean skinTransparencyInExtraPixels = true;
    public boolean enableEnemyTeamPlayersSkinFeatures = true;
    public boolean use3DSkinLayerPatch = true;

    // === Blinking ===
    public boolean enableBlinking = true;
    public int blinkFrequency = 150;
    public int blinkLength = 1;

    // === Misc ===
    public IllegalPathMode illegalPathSupportMode = IllegalPathMode.None;
    public boolean enableFullBodyWardenTextures = true;
    public SettingsButtonLocation configButtonLoc = SettingsButtonLocation.BOTTOM_RIGHT;

    // === OptiFine compat ===
    public boolean optifine_allowWeirdSkipsInTrueRandom = true;
    public boolean optifine_preventBaseTextureInOptifineDirectory = true;

    // === Advanced / debug ===
    public double advanced_IncreaseCacheSizeModifier = 1.0;
    public DebugLogMode debugLoggingMode = DebugLogMode.None;
    public boolean logTextureDataInitialization = false;
    public boolean showDebugHud = false;

    // === Property toggles ===
    public Set<String> propertiesDisabled = new HashSet<>();
    public Set<String> propertyInvertUpdatingOverrides = new HashSet<>();

    // === Per-entity overrides (stored as flat maps to keep JSON round-trip trivial) ===
    public Map<String, Boolean> entityEmissiveOverrides = new HashMap<>();
    public Map<String, Boolean> entityRandomOverrides = new HashMap<>();
    public Map<String, String> entityEmissiveBrightOverrides = new HashMap<>();
    public Map<String, String> entityRenderLayerOverrides = new HashMap<>();
    public Map<String, Integer> entityLightOverrides = new HashMap<>();

    public boolean isPropertyDisabled(RandomProperties.RandomPropertyFactory factory) {
        if (factory == null) return false;
        return propertiesDisabled.contains(factory.getPropertyId());
    }

    public boolean canPropertyUpdate(RandomProperties.RandomPropertyFactory factory) {
        if (factory == null) return true;
        return propertyInvertUpdatingOverrides.contains(factory.getPropertyId()) != factory.updatesOverTime();
    }

    public boolean canDoCustomTextures() {
        return enableCustomTextures;
    }

    public boolean canDoEmissiveTextures() {
        return enableEmissiveTextures;
    }

    public EmissiveRenderModes getEmissiveRenderMode() {
        return emissiveRenderMode;
    }

    // === Enums, ported 1:1 from upstream ===

    public enum UpdateFrequency {
        Never(-1),
        Slow(80),
        Average(20),
        Fast(5),
        Instant(1);

        private final int delay;

        UpdateFrequency(int delay) {
            this.delay = delay;
        }

        public int getDelay() {
            return delay;
        }
    }

    public enum DebugLogMode {
        None,
        Log,
        Chat
    }

    public enum IllegalPathMode {
        None,
        Entity,
        All
    }

    public enum EmissiveRenderModes {
        DULL,
        BRIGHT
    }

    public enum RenderLayerOverride {
        TRANSLUCENT,
        TRANSLUCENT_CULL,
        END,
        OUTLINE
    }

    public enum SettingsButtonLocation {
        OFF,
        BOTTOM_RIGHT,
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_LEFT
    }

    public enum SkinTransparencyMode {
        VANILLA,
        ETF_SKINS_ONLY,
        ALL
    }
}
