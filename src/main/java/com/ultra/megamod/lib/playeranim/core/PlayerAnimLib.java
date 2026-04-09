package com.ultra.megamod.lib.playeranim.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ultra.megamod.lib.playeranim.core.animation.Animation;
import com.ultra.megamod.lib.playeranim.core.loading.AnimationLoader;
import com.ultra.megamod.lib.playeranim.core.loading.KeyFrameLoader;
import com.ultra.megamod.lib.playeranim.core.loading.UniversalAnimLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Map;

public class PlayerAnimLib {
    public static final String MOD_ID = "player_animation_library";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Type ANIMATIONS_MAP_TYPE = new TypeToken<Map<String, Animation>>() {}.getType();
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Animation.Keyframes.class, new KeyFrameLoader())
            .registerTypeAdapter(Animation.class, new AnimationLoader())
            .registerTypeAdapter(ANIMATIONS_MAP_TYPE, new UniversalAnimLoader())
            .disableHtmlEscaping()
            .create();
}
