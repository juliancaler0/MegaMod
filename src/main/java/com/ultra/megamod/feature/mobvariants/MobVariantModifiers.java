package com.ultra.megamod.feature.mobvariants;

import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Modifier types that can be applied to Elite/Champion mobs.
 */
public class MobVariantModifiers {

    public enum Modifier {
        SPEED("Speed") {
            @Override public void apply(Mob mob) {
                mob.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 1, false, true));
            }
        },
        REGEN("Regen") {
            @Override public void apply(Mob mob) {
                mob.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 0, false, true));
            }
        },
        EXPLOSIVE("Explosive") {
            @Override public void apply(Mob mob) {
                // Tagged via PersistentData, handled in MobVariantLoot on death
                mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, true));
            }
        },
        WITHER_TOUCH("Wither Touch") {
            @Override public void apply(Mob mob) {
                // Damage handler checks this modifier tag
                if (mob.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                    double baseDmg = mob.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue();
                    mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(baseDmg * 1.3);
                }
            }
        },
        THORNS("Thorns") {
            @Override public void apply(Mob mob) {
                // Damage handler checks this modifier tag
                if (mob.getAttribute(Attributes.ARMOR) != null) {
                    double baseArmor = mob.getAttribute(Attributes.ARMOR).getBaseValue();
                    mob.getAttribute(Attributes.ARMOR).setBaseValue(baseArmor + 4.0);
                }
            }
        },
        TELEPORTING("Teleporting") {
            @Override public void apply(Mob mob) {
                mob.addEffect(new MobEffectInstance(MobEffects.SPEED, Integer.MAX_VALUE, 0, false, true));
                // Teleport logic handled in MobVariantLoot tick handler
            }
        };

        private final String displayName;

        Modifier(String displayName) {
            this.displayName = displayName;
        }

        public abstract void apply(Mob mob);

        public String getDisplayName() {
            return displayName;
        }
    }

    public static Modifier getRandomModifier(RandomSource random) {
        Modifier[] values = Modifier.values();
        return values[random.nextInt(values.length)];
    }
}
