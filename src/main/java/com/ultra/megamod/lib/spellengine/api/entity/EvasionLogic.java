package com.ultra.megamod.lib.spellengine.api.entity;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.ultra.megamod.lib.spellengine.SpellEngineMod;
import com.ultra.megamod.lib.spellengine.api.event.CombatEvents;
import com.ultra.megamod.lib.spellengine.api.spell.fx.PlayerAnimation;
import com.ultra.megamod.lib.spellengine.api.spell.fx.Sound;
import com.ultra.megamod.lib.spellengine.api.tags.SpellEngineDamageTypeTags;
import com.ultra.megamod.lib.spellengine.fx.SpellEngineSounds;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCast;
import com.ultra.megamod.lib.spellengine.internals.casting.SpellCasterEntity;
import com.ultra.megamod.lib.spellengine.utils.AnimationHelper;
import com.ultra.megamod.lib.spellengine.utils.VectorHelper;

import java.util.Random;

public class EvasionLogic {
    public interface Evader {
        DamageSource getLastEvaded();
        void setLastEvaded(DamageSource source);
    }
    private static final Random RNG = new Random();
    public static boolean tryEvade(LivingEntity entity, float damage, DamageSource source) {
        if (entity.isSleeping()) {
            return false;
        }
        var config = SpellEngineMod.config;
        if (!config.attribute_evasion_allowed_while_spell_casting
                && entity instanceof SpellCasterEntity casterEntity
                && casterEntity.isCastingSpell()) {
            return false;
        }
        if (!config.attribute_evasion_allowed_while_item_usage && entity.isUsingItem()) {
            return false;
        }
        if (source.is(SpellEngineDamageTypeTags.EVADABLE)) {
            var chance = (float)SpellEngineAttributes.EVASION_CHANCE.asChance(entity.getAttributeValue(SpellEngineAttributes.EVASION_CHANCE.entry));

            var angleOfAttack = 0F;
            var evasionAngleLimit = config.attribute_evasion_angle;
            if (evasionAngleLimit > 0F && source.getEntity() != null) {
                var sourcePos = source.getSourcePosition() != null ? source.getSourcePosition() : source.getEntity().position();
                angleOfAttack = (float) VectorHelper.angleBetween(
                        entity.getViewVector(1.0F),
                        new Vec3(sourcePos.x - entity.getX(), 0, sourcePos.z - entity.getZ())
                );
                angleOfAttack = Math.abs(angleOfAttack);
            }
            return chance > 0 && RNG.nextFloat() < chance && angleOfAttack <= evasionAngleLimit;
        }
        return false;
    }

    private static final Sound evadeSound = new Sound("spell_engine:dodge", 1.0F, 1.0F, 0.1F);
    private static final PlayerAnimation evadeAnimation = new PlayerAnimation("spell_engine:dodge");
    public static void onEvade(LivingEntity entity, float damage, DamageSource source) {
        // System.out.println("SpellEngine: " + entity.getName().getString() + " evaded damage from " + source.getName() + "!");
        if (entity instanceof ServerPlayer player) {
            java.util.Collection<net.minecraft.server.level.ServerPlayer> tracker = player.level() instanceof net.minecraft.server.level.ServerLevel sl
                    ? new java.util.ArrayList<>(sl.getChunkSource().chunkMap.getPlayers(player.chunkPosition(), false))
                    : java.util.List.of();
            AnimationHelper.sendAnimation(player, tracker, SpellCast.Animation.MISC, evadeAnimation, 1F);
        }
        entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SpellEngineSounds.DODGE.soundEvent(), entity.getSoundSource(), 1.0F, evadeSound.randomizedPitch());
        CombatEvents.ENTITY_EVASION.invoke(listener -> listener.onEntityEvasion(new CombatEvents.EntityEvasion.Args(entity, damage, source)));
    }
}
