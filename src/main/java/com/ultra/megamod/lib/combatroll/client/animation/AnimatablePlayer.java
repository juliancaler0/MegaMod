package com.ultra.megamod.lib.combatroll.client.animation;

import net.minecraft.world.phys.Vec3;

public interface AnimatablePlayer {
    void playRollAnimation(String animationName, Vec3 direction);
}
