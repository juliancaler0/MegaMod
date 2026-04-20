package com.ultra.megamod.lib.accessories.owo;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
public class Owo {
    public static final String MOD_ID = "owo";
    public static final boolean DEBUG = false;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static void debugWarn(org.slf4j.Logger logger, String msg, Object... args) { if (DEBUG) logger.warn(msg, args); }

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }
}
