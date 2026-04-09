package com.ultra.megamod.feature.mobvariants;

import com.ultra.megamod.feature.economy.EconomyManager;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Handles enhanced loot drops for Elite/Champion mobs, explosive death effect,
 * wither touch damage, thorns reflection, and teleporting AI.
 */
@EventBusSubscriber(modid = "megamod")
public class MobVariantLoot {

    // ---- Enhanced Drops on Death ----

    @SubscribeEvent
    public static void onVariantDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!FeatureToggleManager.get((ServerLevel) event.getEntity().level()).isEnabled("mob_variants")) return;

        LivingEntity entity = event.getEntity();

        String variant = MobVariantManager.getVariant(entity);
        if (variant.isEmpty()) return;

        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) entity.level();

        // Base mob reward multiplier
        String modifiers = MobVariantManager.getModifiers(entity);

        if (variant.equals(MobVariantManager.VARIANT_ELITE)) {
            // Elite: 3x coins bonus + guaranteed iron ingot
            int bonus = 15;
            EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), bonus);
            player.sendSystemMessage(Component.literal("+" + bonus + " MC (Elite Kill)").withStyle(ChatFormatting.YELLOW));
            entity.spawnAtLocation(level, new ItemStack(Items.IRON_INGOT, 2 + level.getRandom().nextInt(3)));
        } else if (variant.equals(MobVariantManager.VARIANT_CHAMPION)) {
            // Champion: 5x coins + rare item + 10% relic fragment chance
            int bonus = 40;
            EconomyManager.get(level.getServer().overworld()).addWallet(player.getUUID(), bonus);
            player.sendSystemMessage(Component.literal("+" + bonus + " MC (Champion Kill!)").withStyle(ChatFormatting.GOLD));
            entity.spawnAtLocation(level, new ItemStack(Items.DIAMOND, 1 + level.getRandom().nextInt(3)));
            entity.spawnAtLocation(level, new ItemStack(Items.GOLDEN_APPLE, 1));
            if (level.getRandom().nextFloat() < 0.10f) {
                entity.spawnAtLocation(level, new ItemStack(Items.NETHER_STAR, 1));
                player.sendSystemMessage(Component.literal("Rare drop: Nether Star!").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        // Explosive modifier: explode on death
        if (modifiers.contains("EXPLOSIVE")) {
            level.explode(null, entity.getX(), entity.getY(), entity.getZ(), 2.0f,
                Level.ExplosionInteraction.NONE);
        }
    }

    // ---- Wither Touch + Thorns on hit ----

    @SubscribeEvent
    public static void onVariantAttack(LivingDamageEvent.Post event) {
        Entity source = event.getSource().getEntity();
        if (source == null || source.level().isClientSide()) return;
        if (!FeatureToggleManager.get((ServerLevel) source.level()).isEnabled("mob_variants")) return;

        // Wither Touch: mob attacking player applies Wither I
        if (source instanceof LivingEntity attacker && event.getEntity() instanceof Player target) {
            String modifiers = MobVariantManager.getModifiers(attacker);
            if (modifiers.contains("WITHER_TOUCH")) {
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WITHER, 60, 0, false, true));
            }
        }

        // Thorns: player attacking mob gets reflected damage
        if (source instanceof Player && event.getEntity() instanceof LivingEntity target) {
            String modifiers = MobVariantManager.getModifiers(target);
            if (modifiers.contains("THORNS")) {
                float reflected = event.getNewDamage() * 0.2f;
                source.hurtServer((ServerLevel) source.level(),
                    source.damageSources().thorns(target), reflected);
            }
        }
    }

    // ---- Teleporting modifier: random teleport every 10s ----

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (!FeatureToggleManager.get(event.getServer().overworld()).isEnabled("mob_variants")) return;
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 200L != 0L) return; // every 10 seconds

        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof LivingEntity living)) continue;
                String modifiers = MobVariantManager.getModifiers(entity);
                if (!modifiers.contains("TELEPORTING")) continue;
                if (!entity.isAlive()) continue;

                // Teleport randomly within 8 blocks
                double tx = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 16;
                double tz = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 16;
                double ty = entity.getY();
                living.randomTeleport(tx, ty, tz, true);
            }
        }
    }
}
