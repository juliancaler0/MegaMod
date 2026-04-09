package com.ultra.megamod.feature.combat.animation.client;

import com.ultra.megamod.lib.playeranim.core.animation.layered.modifier.SpeedModifier;

import java.util.List;

/**
 * Speed modifier with gear-based transmission for upswing/downswing phases.
 * Ported 1:1 from BetterCombat (net.bettercombat.client.animation.TransmissionSpeedModifier).
 * Adapted to MegaMod's PlayerAnimator API.
 */
public class TransmissionSpeedModifier extends SpeedModifier {
    private float elapsed = 0;
    public List<Gear> gears = List.of();

    public TransmissionSpeedModifier(float speed) {
        super(speed);
    }

    public record Gear(float time, float speed) {}

    public void set(float initialSpeed, List<Gear> gears) {
        this.speed = initialSpeed;
        this.gears = gears;
        this.elapsed = 0;
    }

    @Override
    public void tick(com.ultra.megamod.lib.playeranim.core.animation.AnimationData state) {
        super.tick(state);
        this.elapsed += 1;

        // Apply gear shifts based on elapsed time
        for (var gear : gears) {
            if (elapsed >= gear.time) {
                this.speed = gear.speed();
            }
        }
    }
}
