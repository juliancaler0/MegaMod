package com.ultra.megamod.lib.spellengine.client.compatibility;

import com.ultra.megamod.lib.playeranim.core.api.firstPerson.FirstPersonMode;

import com.ultra.megamod.lib.spellengine.client.SpellEngineClient;
import com.ultra.megamod.lib.spellengine.config.TriStateAuto;

public class FirstPersonAnimationCompatibility {
    private static boolean isCameraModPresent = false;

    static void initialize() {
        var cameraMods = new String[] {
            "firstperson", "realcamera"
        };
        for (var mod : cameraMods) {
            if (net.neoforged.fml.ModList.get().isLoaded(mod)) {
                isCameraModPresent = true;
                break;
            }
        }
    }

    public static FirstPersonMode firstPersonMode() {
        switch (SpellEngineClient.config.firstPersonAnimations) {
            case TriStateAuto.YES:
                return FirstPersonMode.THIRD_PERSON_MODEL;
            case TriStateAuto.NO:
                return FirstPersonMode.NONE;
            default:
                return isCameraModPresent ? FirstPersonMode.NONE : FirstPersonMode.THIRD_PERSON_MODEL;
        }
    }
}
