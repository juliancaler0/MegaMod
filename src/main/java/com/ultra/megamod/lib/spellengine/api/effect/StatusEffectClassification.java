package com.ultra.megamod.lib.spellengine.api.effect;


import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.Holder;

import java.util.HashSet;
import java.util.Set;

public class StatusEffectClassification {
    private static final Set<Holder<Attribute>> movementImpairingAttributes = new HashSet<>();
    private static final Set<ResourceKey<MobEffect>> movementImpairingEffects = new HashSet<>();

    public static void init() {
        movementImpairingAttributes.add(Attributes.MOVEMENT_SPEED);
        movementImpairingAttributes.add(Attributes.FLYING_SPEED);
        movementImpairingAttributes.add(Attributes.GRAVITY);
        // Parse immediately - will be called during mod init
        parse(BuiltInRegistries.MOB_EFFECT);
    }

    private static void parse(Registry<MobEffect> registry) {
        // In NeoForge 1.21.11, MobEffect attribute modifier iteration API changed.
        // We manually register known movement impairing effects.
        // Slowness, levitation etc are common movement-impairing effects.
        registry.listElements().forEach(entry -> {
            var key = entry.key();
            var id = key.identifier().toString();
            // Common movement-impairing effects
            if (id.equals("minecraft:slowness") || id.equals("minecraft:levitation") || id.equals("minecraft:weaving")) {
                movementImpairingEffects.add(key);
            }
        });
    }

    public static boolean isMovementImpairing(Holder<MobEffect> effect) {
        var key = effect.unwrapKey();
        if (key.isEmpty()) { // Should never happen, added due to some incompatibility crash
            return false;
        }
        return movementImpairingEffects.contains(key.get());
    }

    public static boolean disablesMobAI(Holder<MobEffect> effectEntry) {
        var effect = effectEntry.value();
        var actionsAllowed = ((ActionImpairing) effect).actionsAllowed();
        if (actionsAllowed == null) {
            return false;
        }
        return !actionsAllowed.mobs().canUseAI();
    }
}
