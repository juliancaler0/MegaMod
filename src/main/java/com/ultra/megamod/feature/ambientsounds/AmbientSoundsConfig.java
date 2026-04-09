package com.ultra.megamod.feature.ambientsounds;

/**
 * Simple config POJO for ambient sounds. Replaces CreativeCore's ICreativeConfig system.
 * Uses static defaults for now -- no file I/O needed.
 */
public class AmbientSoundsConfig {

    public static final AmbientSoundsConfig INSTANCE = new AmbientSoundsConfig();

    /** Master volume multiplier for ambient sounds (0.0 - 1.0) */
    public float volume = 1.0f;

    /** Number of blocks to scan per step during environment analysis */
    public int scanStepAmount = 100;

    /** Whether to start playing sounds at a random position in the file */
    public boolean playSoundWithOffset = true;

    /** Whether to use SoundSource.MASTER instead of SoundSource.AMBIENT */
    public boolean useSoundMasterSource = false;

}
