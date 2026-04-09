package com.ultra.megamod.feature.skills.capstone;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

@EventBusSubscriber(modid = "megamod")
public class MiningCapstones {

    // Recursion guard for Shatter Strike vein mining
    private static boolean isVeinMining = false;

    private static final long VEIN_PULSE_COOLDOWN = 1200L; // 60 seconds

    // ==================== Vein Pulse (ore_finder_4) ====================

    /**
     * On ore break: 15% chance to pulse-scan nearby ores within 8-12 blocks,
     * highlighting each with bright ore-colored particles and a trail from the player.
     * Enhanced (T5): always triggers (30s CD), larger radius, Haste II, Glowing on ores.
     */
    @SubscribeEvent
    public static void onBreakVeinPulse(BlockEvent.BreakEvent event) {
        if (isVeinMining) return;
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "ore_finder_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "ore_finder_5");
        if (!isOre(event.getState().getBlock())) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        long currentTick = serverLevel.getGameTime();

        long cd = enhanced ? 600L : 1200L;
        if (CapstoneManager.isOnCooldown(player.getUUID(), "vein_pulse", currentTick, cd)) {
            return;
        }

        if (!enhanced && player.getRandom().nextFloat() >= 0.15f) return;

        CapstoneManager.setCooldown(player.getUUID(), "vein_pulse", currentTick);

        BlockPos center = event.getPos();
        int radius = enhanced ? 12 : 8;
        int oreCount = 0;
        java.util.List<BlockPos> orePositions = new java.util.ArrayList<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos check = center.offset(dx, dy, dz);
                    BlockState checkState = serverLevel.getBlockState(check);
                    if (isOre(checkState.getBlock())) {
                        oreCount++;
                        orePositions.add(check);
                    }
                }
            }
        }

        if (oreCount > 0) {
            // === Initial pulse burst from the broken block ===
            serverLevel.playSound(null, center, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(),
                    SoundSource.PLAYERS, 0.8f, 1.8f);
            serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, center.getX() + 0.5, center.getY() + 0.5,
                    center.getZ() + 0.5, 1, 0, 0, 0, 0);

            // === Highlight each ore with bright particles and trail from center ===
            double cx = center.getX() + 0.5;
            double cy = center.getY() + 0.5;
            double cz = center.getZ() + 0.5;

            for (BlockPos orePos : orePositions) {
                double ox = orePos.getX() + 0.5;
                double oy = orePos.getY() + 0.5;
                double oz = orePos.getZ() + 0.5;

                // Particle trail from center to ore (breadcrumb path)
                double dist = Math.sqrt((ox - cx) * (ox - cx) + (oy - cy) * (oy - cy) + (oz - cz) * (oz - cz));
                int steps = Math.max(3, (int) (dist * 2));
                for (int s = 1; s <= steps; s++) {
                    double t = (double) s / steps;
                    double px = cx + (ox - cx) * t;
                    double py = cy + (oy - cy) * t;
                    double pz = cz + (oz - cz) * t;
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            px, py, pz, 1, 0.02, 0.02, 0.02, 0.0);
                }

                // Bright highlight burst at the ore location
                Block oreBlock = serverLevel.getBlockState(orePos).getBlock();
                var highlightParticle = getOreParticle(oreBlock);
                serverLevel.sendParticles(highlightParticle,
                        ox, oy, oz, 8, 0.25, 0.25, 0.25, 0.02);
                // Ring of particles around the ore for visibility
                for (int ring = 0; ring < 8; ring++) {
                    double angle = ring * Math.PI * 2.0 / 8.0;
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            ox + Math.cos(angle) * 0.5, oy, oz + Math.sin(angle) * 0.5,
                            1, 0, 0.1, 0, 0.0);
                }

                // Enhanced: make ore blocks glow briefly via Glowing on nearby entities as visual cue
                // Also play a subtle chime per ore cluster
            }

            // Sound for each detected cluster
            serverLevel.playSound(null, center, SoundEvents.AMETHYST_CLUSTER_BREAK,
                    SoundSource.BLOCKS, 1.0f, 0.6f);

            // Actionbar message (not chat spam)
            player.displayClientMessage(Component.literal("\u00A7b\u2726 Vein Pulse: \u00A7f" + oreCount
                    + " \u00A7bore blocks revealed nearby!"), true);

            // Bonus mining XP
            int bonusXp = Math.min(oreCount * 2, 30);
            com.ultra.megamod.feature.skills.SkillManager.get(serverLevel.getServer().overworld())
                    .addXp(player.getUUID(), com.ultra.megamod.feature.skills.SkillTreeType.MINING, bonusXp);

            // Haste buff
            int hasteDuration = enhanced ? 200 : 100;
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.HASTE, hasteDuration, enhanced ? 1 : 0, false, true, true));

            // Enhanced: also grant Night Vision briefly to see the particles better underground
            if (enhanced) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.NIGHT_VISION, 200, 0, false, false, true));
            }
        }
    }

    /**
     * Returns the best particle type to highlight a specific ore — color-coded for recognition.
     */
    private static net.minecraft.core.particles.ParticleOptions getOreParticle(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE)
            return ParticleTypes.SCRAPE;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE)
            return ParticleTypes.HAPPY_VILLAGER;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE)
            return ParticleTypes.WAX_ON;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE)
            return ParticleTypes.CRIT;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE)
            return ParticleTypes.FLAME;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE)
            return ParticleTypes.ENCHANT;
        if (block == Blocks.ANCIENT_DEBRIS)
            return ParticleTypes.SOUL_FIRE_FLAME;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE)
            return ParticleTypes.WAX_OFF;
        return ParticleTypes.ELECTRIC_SPARK;
    }

    // ==================== Shatter Strike (efficient_mining_4) ====================

    /**
     * On ore break: 10% chance to also break all connected ores of the same type (BFS, max 8).
     */
    @SubscribeEvent
    public static void onBreakShatterStrike(BlockEvent.BreakEvent event) {
        if (isVeinMining) return;
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "efficient_mining_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "efficient_mining_5");

        Block brokenBlock = event.getState().getBlock();
        if (!isOre(brokenBlock)) return;

        if (player.getRandom().nextFloat() >= (enhanced ? 0.25f : 0.10f)) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos origin = event.getPos();

        // BFS to find connected ores of same type, max 8 (or 16 if enhanced)
        int maxBlocks = enhanced ? 16 : 8;
        Set<BlockPos> toBreak = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);
        Set<BlockPos> visited = new HashSet<>();
        visited.add(origin);

        while (!queue.isEmpty() && toBreak.size() < maxBlocks) {
            BlockPos current = queue.poll();
            for (BlockPos neighbor : getNeighbors(current)) {
                if (visited.contains(neighbor)) continue;
                visited.add(neighbor);
                BlockState neighborState = serverLevel.getBlockState(neighbor);
                if (neighborState.getBlock() == brokenBlock) {
                    toBreak.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        if (!toBreak.isEmpty()) {
            isVeinMining = true;
            try {
                for (BlockPos pos : toBreak) {
                    serverLevel.destroyBlock(pos, true, player);
                }
            } finally {
                isVeinMining = false;
            }

            serverLevel.playSound(null, origin, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 0.4f, 1.5f);
            serverLevel.sendParticles(ParticleTypes.POOF, origin.getX() + 0.5, origin.getY() + 0.5,
                    origin.getZ() + 0.5, 10, 1.0, 1.0, 1.0, 0.05);
        }
    }

    private static List<BlockPos> getNeighbors(BlockPos pos) {
        return List.of(
                pos.above(), pos.below(),
                pos.north(), pos.south(),
                pos.east(), pos.west()
        );
    }

    // ==================== Perfect Cut (gem_cutter_4) ====================

    /**
     * On diamond/emerald ore break: 5% chance to drop an extra gem.
     */
    @SubscribeEvent
    public static void onBreakPerfectCut(BlockEvent.BreakEvent event) {
        if (isVeinMining) return;
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "gem_cutter_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "gem_cutter_5");

        Block block = event.getState().getBlock();
        ItemStack bonusDrop = getBonusGemDrop(block, enhanced);
        if (bonusDrop.isEmpty()) return;

        if (player.getRandom().nextFloat() >= (enhanced ? 0.20f : 0.10f)) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos pos = event.getPos();

        ItemEntity itemEntity = new ItemEntity(serverLevel,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, bonusDrop);
        serverLevel.addFreshEntity(itemEntity);

        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 1.0f, 1.5f);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.02);

        player.sendSystemMessage(Component.literal("Perfect Cut! Extra gem dropped!")
                .withStyle(ChatFormatting.GREEN));
    }

    private static ItemStack getBonusGemDrop(Block block, boolean enhanced) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
            return new ItemStack(Items.DIAMOND);
        }
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) {
            return new ItemStack(Items.EMERALD);
        }
        if (enhanced) {
            if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) {
                return new ItemStack(Items.LAPIS_LAZULI);
            }
            if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) {
                return new ItemStack(Items.REDSTONE);
            }
        }
        return ItemStack.EMPTY;
    }

    // ==================== Earthen Shield (tunnel_rat_4) ====================

    /**
     * On fall damage: cancel all fall damage. If fall distance > 10, create a shockwave
     * damaging nearby entities.
     */
    @SubscribeEvent
    public static void onFallDamageEarthenShield(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_5");

        // Check if this is fall damage
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) return;

        float fallDistance = (float) player.fallDistance;

        // Cancel all fall damage
        event.setNewDamage(0.0f);

        ServerLevel level = (ServerLevel) player.level();

        // If significant fall, create shockwave
        if (fallDistance > 10.0f) {
            float shockwaveDamage = Math.min(enhanced ? 12.0f : 6.0f, fallDistance * (enhanced ? 0.6f : 0.3f));
            AABB area = player.getBoundingBox().inflate(enhanced ? 6.0 : 3.0);
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, area,
                    e -> e != player && e.isAlive());

            for (LivingEntity target : nearbyEntities) {
                target.hurt(player.damageSources().playerAttack(player), shockwaveDamage);
                // Knockback
                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) {
                    target.push(dx / dist * 0.8, 0.4, dz / dist * 0.8);
                }
            }

            level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND,
                    SoundSource.PLAYERS, 0.8f, 0.6f);
            level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY(),
                    player.getZ(), 3, 1.0, 0.5, 1.0, 0.0);
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY(),
                    player.getZ(), 15, 1.5, 0.2, 1.5, 0.05);
        }
    }

    // ==================== Auto-Smelt (smelter_4) ====================

    /**
     * On ore break: 20% chance to spawn an extra smelted ingot alongside normal drops.
     */
    @SubscribeEvent
    public static void onBreakAutoSmelt(BlockEvent.BreakEvent event) {
        if (isVeinMining) return;
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "smelter_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "smelter_5");

        Block block = event.getState().getBlock();
        ItemStack smeltedItem = getSmeltedResult(block);
        if (smeltedItem.isEmpty()) return;

        if (player.getRandom().nextFloat() >= (enhanced ? 0.40f : 0.20f)) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos pos = event.getPos();

        ItemEntity itemEntity = new ItemEntity(serverLevel,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, smeltedItem);
        serverLevel.addFreshEntity(itemEntity);

        serverLevel.playSound(null, pos, SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS, 0.6f, 1.2f);
        serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
    }

    private static ItemStack getSmeltedResult(Block block) {
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) {
            return new ItemStack(Items.IRON_INGOT);
        }
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) {
            return new ItemStack(Items.GOLD_INGOT);
        }
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) {
            return new ItemStack(Items.COPPER_INGOT);
        }
        if (block == Blocks.ANCIENT_DEBRIS) {
            return new ItemStack(Items.NETHERITE_SCRAP);
        }
        return ItemStack.EMPTY;
    }

    // ==================== Utility ====================

    private static boolean isOre(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE
                || block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE
                || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE
                || block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.NETHER_QUARTZ_ORE || block == Blocks.NETHER_GOLD_ORE
                || block == Blocks.ANCIENT_DEBRIS;
    }
}
