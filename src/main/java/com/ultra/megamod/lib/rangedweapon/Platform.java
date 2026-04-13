package com.ultra.megamod.lib.rangedweapon;

import com.ultra.megamod.lib.rangedweapon.internal.RangedWeaponAttribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.fml.ModList;

/**
 * Platform utility class. Since we're running on NeoForge only inside MegaMod,
 * this class is simplified from the multi-platform original.
 */
public class Platform {
    public static final boolean Fabric = false;
    public static final boolean Forge = false;
    public static final boolean NeoForge = true;

    public enum Type { FABRIC, FORGE, NEOFORGE }

    public static Type getPlatformType() {
        return Type.NEOFORGE;
    }

    public interface Util {
        boolean isModLoaded(String modid);
        RangedAttribute makeAttribute(String translationKey, double fallback, double min, double max);
    }

    private static final Util UTIL = new NeoForgeUtil();
    public static Util util() {
        return UTIL;
    }

    private static class NeoForgeUtil implements Util {
        @Override
        public boolean isModLoaded(String modid) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public RangedAttribute makeAttribute(String translationKey, double fallback, double min, double max) {
            return new RangedWeaponAttribute(translationKey, fallback, min, max);
        }
    }
}
