package com.ultra.megamod.feature.alchemy;

import com.ultra.megamod.MegaMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles right-click use of alchemy potion items.
 * Since we use simple items (not custom Item subclasses), we intercept right-click.
 */
@EventBusSubscriber(modid = MegaMod.MODID)
public class AlchemyPotionUseHandler {

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        ItemStack stack = event.getItemStack();
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        if (!itemId.startsWith("megamod:potion_")) return;

        ServerPlayer serverPlayer = (ServerPlayer) player;

        // Admin bypass — skip skill requirements
        if (!com.ultra.megamod.feature.skills.locks.SkillLockManager.isAdminBypassing(serverPlayer)) {
            // Check skill requirements
            AlchemyRecipeRegistry.BrewingRecipe recipe = AlchemyRecipeRegistry.getBrewingByOutput(itemId);
            if (recipe != null) {
                net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
                com.ultra.megamod.feature.skills.SkillManager skills = com.ultra.megamod.feature.skills.SkillManager.get(level);
                java.util.UUID uuid = player.getUUID();
                int arcaneLevel = skills.getLevel(uuid, com.ultra.megamod.feature.skills.SkillTreeType.ARCANE);
                boolean meetsReq = switch (recipe.tier()) {
                    case 1 -> arcaneLevel >= 5;
                    case 2 -> arcaneLevel >= 10;
                    case 3 -> skills.isNodeUnlocked(uuid, "mana_weaver_1");
                    case 4 -> skills.isNodeUnlocked(uuid, "mana_weaver_3");
                    case 5 -> skills.isNodeUnlocked(uuid, "mana_weaver_5");
                    default -> false;
                };
                if (!meetsReq) {
                    serverPlayer.displayClientMessage(Component.literal("\u00a7cYou need " + AlchemyRecipeRegistry.getTierRequirement(recipe.tier()) + " to use this potion."), true);
                    return;
                }
            }
        }

        // Apply potion effects
        boolean consumed = applyPotionEffects(serverPlayer, itemId);

        if (consumed) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            // Drinking sound
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.GENERIC_DRINK, net.minecraft.sounds.SoundSource.PLAYERS,
                    1.0f, 1.0f);
            serverPlayer.displayClientMessage(Component.literal("\u00a7dYou drink the " +
                    AlchemyRecipeRegistry.getPotionDisplayName(itemId) + "."), true);
        }
    }

    private static boolean applyPotionEffects(ServerPlayer player, String potionId) {
        return switch (potionId) {
            case "megamod:potion_inferno" -> {
                // Fire damage boost + fire resistance (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.INFERNO_BOOST, 600, 0);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0, false, true));
                yield true;
            }
            case "megamod:potion_glacier" -> {
                // Frost aura slows nearby mobs + frost resistance (45s = 900 ticks)
                applyCustomEffect(player, AlchemyRegistry.FROST_AURA, 900, 0);
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 900, 0, false, true));
                yield true;
            }
            case "megamod:potion_shadow_step" -> {
                // Invisibility + Speed II + no footstep sounds (20s = 400 ticks)
                applyCustomEffect(player, AlchemyRegistry.SHADOW_STEP, 400, 0);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 400, 1, false, false));
                yield true;
            }
            case "megamod:potion_vitality" -> {
                // Regeneration III + Absorption II (30s = 600 ticks)
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1, false, true));
                yield true;
            }
            case "megamod:potion_void_walk" -> {
                // No fall damage + levitation control (25s = 500 ticks)
                applyCustomEffect(player, AlchemyRegistry.VOID_WALK, 500, 0);
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 500, 0, false, true));
                yield true;
            }
            case "megamod:potion_tempest" -> {
                // Lightning strikes nearby mobs every 3s (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.TEMPEST, 600, 0);
                yield true;
            }
            case "megamod:potion_berserker" -> {
                // Strength III + -50% defense, attacks heal 10% (20s = 400 ticks)
                applyCustomEffect(player, AlchemyRegistry.BERSERKER_RAGE, 400, 0);
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 400, 2, false, true));
                yield true;
            }
            case "megamod:potion_starlight" -> {
                // Night vision + glowing mobs in 32 blocks + luck (60s = 1200 ticks)
                applyCustomEffect(player, AlchemyRegistry.STARLIGHT, 1200, 0);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 1200, 0, false, true));
                yield true;
            }
            case "megamod:potion_stone_skin" -> {
                // Resistance III + Slowness I + knockback immunity (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.STONE_SKIN, 600, 0);
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 600, 0, false, true));
                yield true;
            }
            case "megamod:potion_arcane_surge" -> {
                // +50% ability power, +30% CDR, mana regen (45s = 900 ticks)
                applyCustomEffect(player, AlchemyRegistry.ARCANE_SURGE, 900, 0);
                yield true;
            }
            case "megamod:potion_swiftbrew" -> {
                // Speed III + Jump Boost II (30s = 600 ticks)
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 600, 2, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 600, 1, false, true));
                yield true;
            }
            case "megamod:potion_iron_gut" -> {
                // Hunger immunity + saturation + poison immunity (120s = 2400 ticks)
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2400, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 2400, 0, false, false)); // Will be overridden by saturation
                yield true;
            }
            case "megamod:potion_midas_touch" -> {
                // +100% MegaCoin drops from mobs (60s = 1200 ticks)
                applyCustomEffect(player, AlchemyRegistry.MIDAS_TOUCH, 1200, 0);
                yield true;
            }
            case "megamod:potion_eagle_eye" -> {
                // +50% ranged damage + no gravity projectiles (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.EAGLE_EYE, 600, 0);
                yield true;
            }
            case "megamod:potion_undying" -> {
                // Totem of undying effect once within 120s = 2400 ticks
                applyCustomEffect(player, AlchemyRegistry.UNDYING_GRACE, 2400, 0);
                yield true;
            }
            case "megamod:potion_phantom" -> {
                // Spectral form: invisibility + speed II + custom phase effect (25s = 500 ticks)
                applyCustomEffect(player, AlchemyRegistry.PHANTOM_PHASE, 500, 0);
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 500, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 500, 1, false, false));
                yield true;
            }
            case "megamod:potion_titan" -> {
                // Extended reach + Strength II + Resistance I (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.TITAN, 600, 0);
                player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 600, 1, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 600, 0, false, true));
                yield true;
            }
            case "megamod:potion_tidal_wave" -> {
                // Water Breathing + Dolphin's Grace + Conduit Power (90s = 1800 ticks)
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1800, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 1800, 0, false, true));
                player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 1800, 0, false, true));
                yield true;
            }
            case "megamod:potion_chronos" -> {
                // Time slow: nearby mobs get Slowness III, player gets Haste II (30s = 600 ticks)
                applyCustomEffect(player, AlchemyRegistry.CHRONOS, 600, 0);
                player.addEffect(new MobEffectInstance(MobEffects.HASTE, 600, 1, false, true));
                yield true;
            }
            case "megamod:potion_blood_rage" -> {
                // Stacking damage on hit + HP drain over time (20s = 400 ticks)
                applyCustomEffect(player, AlchemyRegistry.BLOOD_RAGE, 400, 0);
                yield true;
            }
            case "megamod:potion_spell_arcane_surge" -> {
                // +20 ARCANE_POWER for 3 minutes (3600 ticks)
                applyCustomEffect(player, AlchemyRegistry.SPELL_ARCANE_SURGE, 3600, 0);
                yield true;
            }
            case "megamod:potion_fire_attunement" -> {
                // +20 FIRE_DAMAGE_BONUS for 3 minutes (3600 ticks)
                applyCustomEffect(player, AlchemyRegistry.FIRE_ATTUNEMENT, 3600, 0);
                yield true;
            }
            case "megamod:potion_frost_attunement" -> {
                // +20 ICE_DAMAGE_BONUS for 3 minutes (3600 ticks)
                applyCustomEffect(player, AlchemyRegistry.FROST_ATTUNEMENT, 3600, 0);
                yield true;
            }
            case "megamod:potion_healing_grace" -> {
                // +20 HEALING_POWER for 3 minutes (3600 ticks)
                applyCustomEffect(player, AlchemyRegistry.HEALING_GRACE, 3600, 0);
                yield true;
            }
            default -> false;
        };
    }

    private static void applyCustomEffect(ServerPlayer player, java.util.function.Supplier<MobEffect> effectSupplier, int duration, int amplifier) {
        try {
            Holder<MobEffect> holder = AlchemyRegistry.holderOf(effectSupplier);
            player.addEffect(new MobEffectInstance(holder, duration, amplifier, false, true));
        } catch (Exception e) {
            MegaMod.LOGGER.error("Failed to apply alchemy effect", e);
        }
    }
}
