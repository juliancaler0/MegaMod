package com.ultra.megamod.lib.spellengine.client.compatibility;

public class CompatFeatures {
    public static void initialize() {
        FirstPersonAnimationCompatibility.initialize();
        ShaderCompatibility.initialize();
    }
}
