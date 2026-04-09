/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.bus.api.SubscribeEvent
 *  net.neoforged.fml.common.EventBusSubscriber
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Post
 *  net.neoforged.neoforge.event.entity.living.LivingDamageEvent$Pre
 *  net.neoforged.neoforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.neoforged.neoforge.event.tick.ServerTickEvent$Post
 */
package com.ultra.megamod.feature.attributes;

import com.ultra.megamod.feature.attributes.AttributeHelper;
import com.ultra.megamod.feature.attributes.MegaModAttributes;
import com.ultra.megamod.feature.attributes.network.CombatTextSender;
import com.ultra.megamod.feature.computer.network.handlers.SettingsHandler;
import com.ultra.megamod.feature.hud.DamageHistoryTracker;
import com.ultra.megamod.feature.hud.network.ScreenShakePayload;
import com.ultra.megamod.feature.skills.synergy.SynergyEffects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.ArrayList;

@EventBusSubscriber(modid="megamod")
public class AttributeEvents {
    private static final Identifier COMBO_SPEED_MODIFIER = Identifier.fromNamespaceAndPath("megamod", "combo_speed");
    private static final Identifier JUMP_HEIGHT_MODIFIER = Identifier.fromNamespaceAndPath("megamod", "jump_height_bonus");
    private static final Identifier SWIM_SPEED_MODIFIER = Identifier.fromNamespaceAndPath("megamod", "swim_speed_bonus");
    private static final Identifier EXCAVATION_REACH_MODIFIER = Identifier.fromNamespaceAndPath("megamod", "excavation_reach");
    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        float damage = event.getNewDamage();
        if (event.getSource().is(DamageTypes.FALL)) {
            double multiplier;
            double fallReduction = AttributeHelper.getValue(target, MegaModAttributes.FALL_DAMAGE_REDUCTION);
            if (fallReduction > 0.0 && (damage *= (float)Math.max(0.0, multiplier = 1.0 - fallReduction / 100.0)) <= 0.0f) {
                event.setNewDamage(0.0f);
                return;
            }
            event.setNewDamage(damage);
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof LivingEntity)) {
            event.setNewDamage(damage);
            return;
        }
        LivingEntity attacker = (LivingEntity)entity;

        // Bow damage bonus: apply loot-rolled attack_damage from the bow to arrow projectile damage
        Entity directEntity = event.getSource().getDirectEntity();
        if (directEntity instanceof net.minecraft.world.entity.projectile.arrow.AbstractArrow arrow) {
            ItemStack weapon = arrow.getWeaponItem();
            if (weapon != null && !weapon.isEmpty()) {
                var attrMods = weapon.get(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS);
                if (attrMods != null) {
                    for (var modEntry : attrMods.modifiers()) {
                        if (modEntry.attribute().is(Attributes.ATTACK_DAMAGE)
                                && modEntry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                            damage += (float) modEntry.modifier().amount();
                        }
                    }
                }
            }

            // RANGED_DAMAGE attribute: percentage bonus to all projectile damage
            double rangedBonus = AttributeHelper.getValue(attacker, MegaModAttributes.RANGED_DAMAGE);
            if (rangedBonus > 0.0) {
                damage += damage * (float)(rangedBonus / 100.0);
            }
        }

        double dodgeChance = AttributeHelper.getValue(target, MegaModAttributes.DODGE_CHANCE);
        if (dodgeChance > 0.0 && target.getRandom().nextDouble() * 100.0 < dodgeChance) {
            event.setNewDamage(0.0f);
            CombatTextSender.sendDodge(target);
            // Audio feedback for the dodging player
            target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_ATTACK_SWEEP, target.getSoundSource(), 0.5f, 1.5f);
            return;
        }
        boolean didCrit = false;
        double critChance = AttributeHelper.getValue(attacker, MegaModAttributes.CRITICAL_CHANCE);
        double critDamage = AttributeHelper.getValue(attacker, MegaModAttributes.CRITICAL_DAMAGE);
        if (critChance > 0.0 && attacker.getRandom().nextDouble() * 100.0 < critChance) {
            double critMultiplier = 1.0 + critDamage / 100.0;
            damage *= (float)critMultiplier;
            didCrit = true;
            CombatTextSender.sendCrit(target, damage);
            // Tag target for crit_kill challenge tracking
            target.getPersistentData().putBoolean("megamod_was_crit", true);
        }
        double fireDmg = AttributeHelper.getValue(attacker, MegaModAttributes.FIRE_DAMAGE_BONUS);
        double iceDmg = AttributeHelper.getValue(attacker, MegaModAttributes.ICE_DAMAGE_BONUS);
        double lightningDmg = AttributeHelper.getValue(attacker, MegaModAttributes.LIGHTNING_DAMAGE_BONUS);
        double poisonDmg = AttributeHelper.getValue(attacker, MegaModAttributes.POISON_DAMAGE_BONUS);
        double holyDmg = AttributeHelper.getValue(attacker, MegaModAttributes.HOLY_DAMAGE_BONUS);
        double shadowDmg = AttributeHelper.getValue(attacker, MegaModAttributes.SHADOW_DAMAGE_BONUS);
        double fireRes = AttributeHelper.getValue(target, MegaModAttributes.FIRE_RESISTANCE_BONUS);
        double iceRes = AttributeHelper.getValue(target, MegaModAttributes.ICE_RESISTANCE_BONUS);
        double lightningRes = AttributeHelper.getValue(target, MegaModAttributes.LIGHTNING_RESISTANCE_BONUS);
        double poisonRes = AttributeHelper.getValue(target, MegaModAttributes.POISON_RESISTANCE_BONUS);
        double holyRes = AttributeHelper.getValue(target, MegaModAttributes.HOLY_RESISTANCE_BONUS);
        double shadowRes = AttributeHelper.getValue(target, MegaModAttributes.SHADOW_RESISTANCE_BONUS);
        // Brilliance amplifies all elemental damage dealt
        double brilliance = AttributeHelper.getValue(attacker, MegaModAttributes.BRILLIANCE);
        double brillianceMultiplier = 1.0 + brilliance / 100.0;
        fireDmg *= brillianceMultiplier;
        iceDmg *= brillianceMultiplier;
        lightningDmg *= brillianceMultiplier;
        poisonDmg *= brillianceMultiplier;
        holyDmg *= brillianceMultiplier;
        shadowDmg *= brillianceMultiplier;
        // Runic Arsenal synergy: enchanted weapons deal bonus elemental damage
        if (attacker instanceof ServerPlayer sp) {
            float runicMultiplier = SynergyEffects.getRunicArsenalMultiplier(sp);
            if (runicMultiplier > 1.0f) {
                fireDmg *= runicMultiplier;
                iceDmg *= runicMultiplier;
                lightningDmg *= runicMultiplier;
                poisonDmg *= runicMultiplier;
                holyDmg *= runicMultiplier;
                shadowDmg *= runicMultiplier;
            }
        }

        double totalElemental = 0.0;
        double fireActual = fireDmg * Math.max(0.0, 1.0 - fireRes / 100.0);
        totalElemental += fireActual;
        if (fireActual > 0.0) CombatTextSender.sendElemental(target, (float) fireActual, "fire");
        double iceActual = iceDmg * Math.max(0.0, 1.0 - iceRes / 100.0);
        totalElemental += iceActual;
        if (iceActual > 0.0) CombatTextSender.sendElemental(target, (float) iceActual, "ice");
        double lightningActual = lightningDmg * Math.max(0.0, 1.0 - lightningRes / 100.0);
        totalElemental += lightningActual;
        if (lightningActual > 0.0) CombatTextSender.sendElemental(target, (float) lightningActual, "lightning");
        double poisonActual = poisonDmg * Math.max(0.0, 1.0 - poisonRes / 100.0);
        totalElemental += poisonActual;
        if (poisonActual > 0.0) CombatTextSender.sendElemental(target, (float) poisonActual, "poison");
        double holyActual = holyDmg * Math.max(0.0, 1.0 - holyRes / 100.0);
        totalElemental += holyActual;
        if (holyActual > 0.0) CombatTextSender.sendElemental(target, (float) holyActual, "holy");
        double shadowActual = shadowDmg * Math.max(0.0, 1.0 - shadowRes / 100.0);
        totalElemental += shadowActual;
        if (shadowActual > 0.0) CombatTextSender.sendElemental(target, (float) shadowActual, "shadow");
        damage += (float) totalElemental;
        double armorShred = AttributeHelper.getValue(attacker, MegaModAttributes.ARMOR_SHRED);
        if (armorShred > 0.0) {
            double shredMultiplier = 1.0 + armorShred / 200.0;
            damage *= (float)shredMultiplier;
        }
        damage = Math.max(0.0f, damage);
        event.setNewDamage(damage);
        if (damage > 0.0f && !didCrit) {
            CombatTextSender.sendDamage(target, damage);
        }
        // Screen shake on heavy hits
        if (target instanceof ServerPlayer sp && damage > sp.getMaxHealth() * 0.25f) {
            if (SettingsHandler.isEnabled(sp.getUUID(), "fx_screen_shake")) {
                float intensity = Math.min(1.0f, damage / sp.getMaxHealth());
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(sp, new ScreenShakePayload(intensity));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamagePost(LivingDamageEvent.Post event) {
        double stunChance;
        float reflectedDamage;
        double thorns;
        float healAmount;
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        float damageDealt = event.getNewDamage();
        if (damageDealt <= 0.0f) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity)entity;
        ServerLevel serverLevel = (ServerLevel)target.level();
        // Record damage for death recap
        if (target instanceof ServerPlayer sp) {
            String sourceName = attacker.getName().getString();
            String dmgType = event.getSource().type().msgId();
            DamageHistoryTracker.record(sp, sourceName, dmgType, damageDealt);
        }
        double lifesteal = AttributeHelper.getValue(attacker, MegaModAttributes.LIFESTEAL);
        if (lifesteal > 0.0 && (healAmount = (float)((double)damageDealt * lifesteal / 100.0)) > 0.0f) {
            // Bloodblade synergy: amplify lifesteal healing
            if (attacker instanceof ServerPlayer sp) {
                healAmount *= SynergyEffects.getLifestealMultiplier(sp);
            }
            attacker.heal(healAmount);
            CombatTextSender.sendLifesteal(attacker, healAmount);
        }
        if ((thorns = AttributeHelper.getValue(target, MegaModAttributes.THORNS_DAMAGE)) > 0.0 && (reflectedDamage = (float)((double)damageDealt * thorns / 100.0)) > 0.0f) {
            attacker.hurtServer(serverLevel, target.damageSources().thorns((Entity)target), reflectedDamage);
            CombatTextSender.sendThorns(attacker, reflectedDamage);
        }
        if ((stunChance = AttributeHelper.getValue(attacker, MegaModAttributes.STUN_CHANCE)) > 0.0 && attacker.getRandom().nextDouble() * 100.0 < stunChance) {
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 3, false, true, true));
            target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 30, 2, false, true, true));
            CombatTextSender.sendStun(target);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        double miningBonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.MINING_SPEED_BONUS);
        if (miningBonus > 0.0) {
            float originalSpeed = event.getOriginalSpeed();
            float bonusMultiplier = 1.0f + (float)(miningBonus / 100.0);
            event.setNewSpeed(originalSpeed * bonusMultiplier);
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (event.getBlocked() && event.getBlockedDamage() > 0) {
            CombatTextSender.sendBlock(event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().overworld().getGameTime() % 20L != 0L) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player.isDeadOrDying()) continue;

            // Health regen bonus
            double regenBonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.HEALTH_REGEN_BONUS);
            if (regenBonus > 0.0 && player.getHealth() < player.getMaxHealth()) {
                float before = player.getHealth();
                player.heal((float)regenBonus);
                float healed = player.getHealth() - before;
                if (healed > 0.1f) {
                    CombatTextSender.sendLifesteal(player, healed);
                }
            }

            // Hunger efficiency — reduce exhaustion to slow hunger drain
            double hungerEfficiency = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.HUNGER_EFFICIENCY);
            if (hungerEfficiency > 0.0) {
                // Counteract exhaustion by adding negative exhaustion proportional to efficiency
                float reduction = (float)(hungerEfficiency / 100.0) * 0.5f;
                player.getFoodData().addExhaustion(-reduction);
            }

            // Combo speed — apply transient attack speed modifier
            double comboSpeed = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.COMBO_SPEED);
            if (comboSpeed > 0.0) {
                if (!AttributeHelper.hasModifier((LivingEntity)player, Attributes.ATTACK_SPEED, COMBO_SPEED_MODIFIER)) {
                    AttributeHelper.addModifier((LivingEntity)player, Attributes.ATTACK_SPEED,
                        COMBO_SPEED_MODIFIER, comboSpeed / 100.0,
                        AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                }
            } else {
                AttributeHelper.removeModifier((LivingEntity)player, Attributes.ATTACK_SPEED, COMBO_SPEED_MODIFIER);
            }

            // Jump height bonus — always refresh so value updates when skills change
            double jumpBonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.JUMP_HEIGHT_BONUS);
            AttributeHelper.removeModifier((LivingEntity)player, Attributes.JUMP_STRENGTH, JUMP_HEIGHT_MODIFIER);
            if (jumpBonus > 0.0) {
                AttributeHelper.addModifier((LivingEntity)player, Attributes.JUMP_STRENGTH,
                    JUMP_HEIGHT_MODIFIER, jumpBonus / 10.0,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }

            // Swim speed bonus — always refresh so value updates when skills change
            double swimBonus = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.SWIM_SPEED_BONUS);
            AttributeHelper.removeModifier((LivingEntity)player, Attributes.WATER_MOVEMENT_EFFICIENCY, SWIM_SPEED_MODIFIER);
            if (swimBonus > 0.0) {
                AttributeHelper.addModifier((LivingEntity)player, Attributes.WATER_MOVEMENT_EFFICIENCY,
                    SWIM_SPEED_MODIFIER, swimBonus / 100.0,
                    AttributeModifier.Operation.ADD_VALUE);
            }

            // Excavation reach — always refresh so value updates when skills change
            double excReach = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.EXCAVATION_REACH);
            AttributeHelper.removeModifier((LivingEntity)player, Attributes.BLOCK_INTERACTION_RANGE, EXCAVATION_REACH_MODIFIER);
            if (excReach > 0.0) {
                AttributeHelper.addModifier((LivingEntity)player, Attributes.BLOCK_INTERACTION_RANGE,
                    EXCAVATION_REACH_MODIFIER, excReach,
                    AttributeModifier.Operation.ADD_VALUE);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        Entity killer = event.getSource().getEntity();
        if (killer == null || killer.level().isClientSide()) return;
        if (!(killer instanceof Player player)) return;

        double lootFortune = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.LOOT_FORTUNE);
        if (lootFortune <= 0.0) return;

        double chance = lootFortune / 100.0;
        ArrayList<ItemEntity> bonusDrops = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            if (player.getRandom().nextDouble() < chance) {
                ItemStack copy = drop.getItem().copy();
                ItemEntity bonus = new ItemEntity(
                    drop.level(), drop.getX(), drop.getY(), drop.getZ(), copy
                );
                bonus.setDefaultPickUpDelay();
                bonusDrops.add(bonus);
            }
        }
        event.getDrops().addAll(bonusDrops);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide()) return;

        double lootFortune = AttributeHelper.getValue((LivingEntity)player, MegaModAttributes.LOOT_FORTUNE);
        if (lootFortune <= 0.0) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        net.minecraft.world.level.block.state.BlockState state = event.getState();
        net.minecraft.core.BlockPos pos = event.getPos();

        // Non-admin players: only duplicate ore blocks
        boolean isAdmin = player instanceof net.minecraft.server.level.ServerPlayer sp
                && com.ultra.megamod.feature.computer.admin.AdminSystem.isAdmin(sp);
        if (!isAdmin && !state.is(net.minecraft.tags.BlockTags.GOLD_ORES)
                && !state.is(net.minecraft.tags.BlockTags.IRON_ORES)
                && !state.is(net.minecraft.tags.BlockTags.COPPER_ORES)
                && !state.is(net.minecraft.tags.BlockTags.COAL_ORES)
                && !state.is(net.minecraft.tags.BlockTags.LAPIS_ORES)
                && !state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES)
                && !state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES)
                && !state.is(net.minecraft.tags.BlockTags.EMERALD_ORES)
                && !state.getBlock().equals(net.minecraft.world.level.block.Blocks.NETHER_QUARTZ_ORE)
                && !state.getBlock().equals(net.minecraft.world.level.block.Blocks.ANCIENT_DEBRIS)) {
            return;
        }

        // Get expected drops from the block
        java.util.List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
            state, serverLevel, pos, null, player, player.getMainHandItem()
        );

        double chance = lootFortune / 100.0;
        for (ItemStack drop : drops) {
            if (player.getRandom().nextDouble() < chance) {
                ItemStack copy = drop.copy();
                ItemEntity bonus = new ItemEntity(
                    serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, copy
                );
                bonus.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(bonus);
            }
        }
    }
}

