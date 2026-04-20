package com.ultra.megamod.lib.accessories.owo.ui.util;

import com.ultra.megamod.lib.accessories.owo.Owo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class UISounds {

    public static final SoundEvent UI_INTERACTION = SoundEvent.createVariableRangeEvent(Owo.id("ui.owo.interaction"));

    private UISounds() {}

    public static void play(SoundEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1));
    }

    public static void playButtonSound() {
        play(SoundEvents.UI_BUTTON_CLICK.value());
    }

    public static void playInteractionSound() {
        play(UI_INTERACTION);
    }
}
