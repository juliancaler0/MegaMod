package com.ultra.megamod.lib.playeranim.core.util;

import com.google.gson.JsonObject;
import com.ultra.megamod.lib.playeranim.core.PlayerAnimLib;

public class ParticleEffectUtils {
    public static String parseIdentifier(String raw) {
        return getIdentifier(PlayerAnimLib.GSON.fromJson(raw, JsonObject.class));
    }

    public static String getIdentifier(JsonObject obj) {
        return obj.getAsJsonObject("particle_effect")
                .getAsJsonObject("description")
                .get("identifier").getAsString();
    }
}
