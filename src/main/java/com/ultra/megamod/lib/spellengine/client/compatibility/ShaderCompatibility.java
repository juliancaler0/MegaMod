package com.ultra.megamod.lib.spellengine.client.compatibility;

import java.util.function.Supplier;

public class ShaderCompatibility {
    private static Supplier<Boolean> shaderPackInUse = () -> false;
    private static boolean vanillaRenderSystem = true;
    static void initialize() {
        // Iris/Oculus shader detection not available in this environment
        // Default to vanilla render system
    }
    public static boolean isShaderPackInUse() {
        return shaderPackInUse.get();
    }
    public static boolean isVanillaRenderSystem() {
        return vanillaRenderSystem;
    }
}
