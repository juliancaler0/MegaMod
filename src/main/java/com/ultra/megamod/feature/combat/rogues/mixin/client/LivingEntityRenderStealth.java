package com.ultra.megamod.feature.combat.rogues.mixin.client;

/**
 * Stealth rendering documentation.
 * Ported from net.rogues.mixin.client.LivingEntityRenderStealth.
 *
 * In the Fabric source, this mixin on LivingEntityRenderer provided:
 * 1. Translucent render type for stealthed entities visible to the local player
 * 2. Reduced alpha (15%) on the entity model
 * 3. Suppressed feature layers except held items
 *
 * In MegaMod 1.21.11:
 * - The STEALTH effect sets the entity as invisible via
 *   {@link com.ultra.megamod.feature.combat.rogues.mixin.LivingEntityStealth}
 *   which hooks into updateInvisibilityStatus and getVisibilityPercent
 * - Invisible entities are already rendered with translucent/ghost rendering
 *   by vanilla Minecraft for players on the same team
 * - The SpellEffectLayerRenderer provides visual particle feedback
 *
 * Stealth gameplay mechanics (mob targeting, attack break) are handled by:
 * - {@link com.ultra.megamod.feature.combat.spell.StealthHandler} (NeoForge events)
 * - {@link com.ultra.megamod.feature.combat.rogues.mixin.LivingEntityStealth} (visibility)
 * - {@link com.ultra.megamod.feature.combat.rogues.mixin.TrackTargetGoalStealth} (AI)
 */
public class LivingEntityRenderStealth {
    // Visual stealth rendering is handled by the vanilla invisibility system,
    // triggered by LivingEntityStealth mixin hooking updateInvisibilityStatus.
}
