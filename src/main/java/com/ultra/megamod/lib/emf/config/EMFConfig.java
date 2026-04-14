package com.ultra.megamod.lib.emf.config;

/**
 * Serializable config record for the EMF port. Mirrors every upstream
 * {@code EMFConfig} field that the Phase F config screen exposes.
 * <p>
 * Stored as JSON at {@code <gamedir>/config/megamod-emf.json}. Hand-editable.
 * Stonecutter-specific fields (physics mod patch, MC<12100 floor UVs) are
 * omitted — they don't apply to the 1.21.11-NeoForge runtime this port targets.
 * Deprecated upstream fields kept only if the runtime still honours them.
 */
@SuppressWarnings("CanBeFinal")
public class EMFConfig {

    // --- Debug / logging --------------------------------------------------
    public boolean logModelCreationData = false;
    public boolean debugOnRightClick = false;
    public boolean onlyDebugRenderOnHover = false;
    public boolean showReloadErrorToast = true;
    public boolean exportRotations = false;
    public boolean showDebugHud = false;

    // --- Render mode ------------------------------------------------------
    public RenderModeChoice renderModeChoice = RenderModeChoice.NORMAL;
    public VanillaModelRenderMode vanillaModelHologramRenderMode = VanillaModelRenderMode.OFF;
    public ModelPrintMode modelExportMode = ModelPrintMode.NONE;

    // --- Update frequency -------------------------------------------------
    public UpdateFrequency modelUpdateFrequency = UpdateFrequency.AVERAGE;

    // --- Player behaviour -------------------------------------------------
    public boolean preventFirstPersonHandAnimating = false;
    public boolean onlyClientPlayerModel = false;
    public boolean resetPlayerModelEachRender = true;

    // --- Performance ------------------------------------------------------
    public int animationLODDistance = 20;
    public boolean retainDetailOnLowFps = true;
    public boolean retainDetailOnLargerMobs = true;
    public boolean animationFrameSkipDuringIrisShadowPass = true;
    public boolean allowEBEModConfigModify = true;
    public boolean doubleChestAnimFix = true;

    // --- OptiFine compat --------------------------------------------------
    public boolean enforceOptifineVariationRequiresDefaultModel = false;
    public boolean enforceOptifineSubFoldersVariantOnly = true;
    public boolean enforceOptiFineAnimSyntaxLimits = true;
    public boolean allowOptifineFallbackProperties = true;

    // --- Disabled models --------------------------------------------------
    /** Explicit filename-based disable list (e.g. {@code "creeper.jem"}). */
    public java.util.Set<String> modelsNamesDisabled = new java.util.HashSet<>();

    public boolean isModelDisabled(String modelName) {
        return modelsNamesDisabled != null && modelsNamesDisabled.contains(modelName);
    }

    /** Entity variant re-evaluation cadence. Mirrors ETF's matching enum. */
    public enum UpdateFrequency {
        INSTANT(1),
        FAST(5),
        AVERAGE(20),
        SLOW(80),
        NEVER(Integer.MAX_VALUE);

        public final int ticksBetweenUpdates;

        UpdateFrequency(int ticksBetweenUpdates) {
            this.ticksBetweenUpdates = ticksBetweenUpdates;
        }
    }

    public enum ModelPrintMode {
        NONE,
        LOG_ONLY,
        LOG_AND_JEM,
        ALL_LOG_ONLY,
        ALL_LOG_AND_JEM;

        public boolean doesJems() {
            return this == LOG_AND_JEM || this == ALL_LOG_AND_JEM;
        }

        public boolean doesAll() {
            return this == ALL_LOG_ONLY || this == ALL_LOG_AND_JEM;
        }

        public boolean doesLog() {
            return this != NONE;
        }
    }

    public enum VanillaModelRenderMode {
        OFF,
        NORMAL,
        OFFSET
    }

    public enum RenderModeChoice {
        NORMAL,
        GREEN,
        LINES_AND_TEXTURE,
        LINES_AND_TEXTURE_FLASH,
        LINES,
        NONE
    }
}
