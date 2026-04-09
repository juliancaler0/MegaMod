package com.ultra.megamod.feature.skills.capstone;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = "megamod")
public class FarmingCapstones {

    // ==================== Golden Harvest (crop_master_4) ====================

    /**
     * On crop break: 15% chance to apply bone meal to a random nearby crop in 3-block radius.
     * Also doubles the chance of extra drops by spawning an extra seed/crop item.
     */
    @SubscribeEvent
    public static void onBreakGoldenHarvest(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "crop_master_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "crop_master_5");

        Block block = event.getState().getBlock();
        if (!isCrop(block)) return;

        if (player.getRandom().nextFloat() >= (enhanced ? 0.30f : 0.15f)) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos center = event.getPos();

        // Find a nearby crop to accelerate growth
        int radius = 3;
        int cropsToGrow = enhanced ? 2 : 1;
        int cropsGrown = 0;
        java.util.Set<BlockPos> grownPositions = new java.util.HashSet<>();

        for (int pass = 0; pass < cropsToGrow; pass++) {
            BlockPos bestCrop = null;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos check = center.offset(dx, dy, dz);
                        if (grownPositions.contains(check)) continue;
                        BlockState checkState = serverLevel.getBlockState(check);
                        if (checkState.getBlock() instanceof CropBlock cropBlock) {
                            if (!cropBlock.isMaxAge(checkState)) {
                                bestCrop = check;
                                break;
                            }
                        }
                    }
                    if (bestCrop != null) break;
                }
                if (bestCrop != null) break;
            }

            if (bestCrop != null) {
                BlockState cropState = serverLevel.getBlockState(bestCrop);
                if (cropState.getBlock() instanceof CropBlock cropBlock) {
                    // Advance crop by 1 age stage
                    int currentAge = cropBlock.getAge(cropState);
                    int maxAge = cropBlock.getMaxAge();
                    if (currentAge < maxAge) {
                        serverLevel.setBlock(bestCrop, cropBlock.getStateForAge(currentAge + 1), 2);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                bestCrop.getX() + 0.5, bestCrop.getY() + 0.5, bestCrop.getZ() + 0.5,
                                5, 0.3, 0.3, 0.3, 0.02);
                        grownPositions.add(bestCrop);
                        cropsGrown++;
                    }
                }
            }
        }

        // Also spawn an extra seed item from the broken crop
        ItemStack bonusSeed = getCropBonusDrop(block);
        if (!bonusSeed.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(serverLevel,
                    center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, bonusSeed);
            serverLevel.addFreshEntity(itemEntity);
        }

        serverLevel.playSound(null, center, SoundEvents.CROP_BREAK,
                SoundSource.BLOCKS, 0.8f, 1.5f);
    }

    private static ItemStack getCropBonusDrop(Block block) {
        if (block == Blocks.WHEAT) return new ItemStack(Items.WHEAT);
        if (block == Blocks.CARROTS) return new ItemStack(Items.CARROT);
        if (block == Blocks.POTATOES) return new ItemStack(Items.POTATO);
        if (block == Blocks.BEETROOTS) return new ItemStack(Items.BEETROOT);
        if (block == Blocks.MELON) return new ItemStack(Items.MELON_SLICE);
        if (block == Blocks.PUMPKIN) return new ItemStack(Items.PUMPKIN);
        return ItemStack.EMPTY;
    }

    private static boolean isCrop(Block block) {
        return block instanceof CropBlock
                || block == Blocks.MELON || block == Blocks.PUMPKIN
                || block == Blocks.SWEET_BERRY_BUSH || block == Blocks.SUGAR_CANE
                || block == Blocks.NETHER_WART || block == Blocks.COCOA;
    }

    // ==================== Beast Bond (animal_handler_4) ====================

    /**
     * Every 100 ticks: apply Resistance I to tamed animals within 16 blocks of players with this node.
     */
    @SubscribeEvent
    public static void onServerTickBeastBond(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "animal_handler_4")) continue;
            boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "animal_handler_5");

            ServerLevel level = (ServerLevel) player.level();
            AABB area = player.getBoundingBox().inflate(16.0);
            List<TamableAnimal> tamed = level.getEntitiesOfClass(TamableAnimal.class, area,
                    animal -> animal.isTame() && animal.isOwnedBy(player));

            for (TamableAnimal animal : tamed) {
                animal.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0, false, false, true));
                if (enhanced) {
                    animal.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 120, 0, false, false, true));
                }
            }
        }
    }

    /**
     * On baby spawn: if caused by a player with Beast Bond, 50% chance to spawn a second baby (twins).
     */
    @SubscribeEvent
    public static void onBabySpawnBeastBond(BabyEntitySpawnEvent event) {
        Player player = event.getCausedByPlayer();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!CapstoneManager.hasCapstoneTrigger(serverPlayer, "animal_handler_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(serverPlayer, "animal_handler_5");

        if (serverPlayer.getRandom().nextFloat() >= (enhanced ? 0.75f : 0.50f)) return;

        Entity child = event.getChild();
        if (child == null) return;

        ServerLevel level = (ServerLevel) serverPlayer.level();

        // Spawn a twin by creating another entity of the same type
        Entity twin = child.getType().create(level, EntitySpawnReason.EVENT);
        if (twin != null) {
            twin.setPos(child.getX() + 0.5, child.getY(), child.getZ() + 0.5);
            twin.setYRot(child.getYRot());
            twin.setXRot(child.getXRot());
            if (twin instanceof LivingEntity livingTwin) {
                if (livingTwin instanceof net.minecraft.world.entity.AgeableMob ageableTwin) {
                    ageableTwin.setAge(-24000); // Baby
                }
            }
            level.addFreshEntity(twin);

            level.playSound(null, child.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                    SoundSource.NEUTRAL, 0.8f, 1.5f);
            serverPlayer.sendSystemMessage(Component.literal("Beast Bond: Twins!")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    // ==================== Nature's Bounty (botanist_4) ====================

    /**
     * On food consumption: if player has this node, restore 1 extra hunger.
     * 10% chance for a random 30s buff.
     */
    @SubscribeEvent
    public static void onFoodEatNaturesBounty(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!(entity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "botanist_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(player, "botanist_5");

        ItemStack item = event.getItem();
        if (!item.has(net.minecraft.core.component.DataComponents.FOOD)) return;

        // Restore 2 extra hunger + saturation
        player.getFoodData().eat(2, 0.5f);

        // 10% (or 25% if enhanced) chance for a random buff
        if (player.getRandom().nextFloat() < (enhanced ? 0.25f : 0.10f)) {
            MobEffectInstance buff = getRandomBountyBuff(player, enhanced);
            player.addEffect(buff);

            ServerLevel level = (ServerLevel) player.level();
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP,
                    SoundSource.PLAYERS, 0.5f, 1.5f);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0,
                    player.getZ(), 8, 0.3, 0.5, 0.3, 0.02);

            player.sendSystemMessage(Component.literal("Nature's Bounty: You feel invigorated!")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    private static MobEffectInstance getRandomBountyBuff(ServerPlayer player, boolean enhanced) {
        int duration = enhanced ? 1200 : 600;
        int roll = player.getRandom().nextInt(4);
        return switch (roll) {
            case 0 -> new MobEffectInstance(MobEffects.SPEED, duration, 0, false, true, true);
            case 1 -> new MobEffectInstance(MobEffects.REGENERATION, duration, 0, false, true, true);
            case 2 -> new MobEffectInstance(MobEffects.RESISTANCE, duration, 0, false, true, true);
            case 3 -> new MobEffectInstance(MobEffects.STRENGTH, duration, 0, false, true, true);
            default -> new MobEffectInstance(MobEffects.SPEED, duration, 0, false, true, true);
        };
    }

    // ==================== Master Chef (cook_4) ====================

    /**
     * On crafting food: 25% chance to add 1 extra item to the stack.
     */
    @SubscribeEvent
    public static void onCraftMasterChef(PlayerEvent.ItemCraftedEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!CapstoneManager.hasCapstoneTrigger(serverPlayer, "cook_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(serverPlayer, "cook_5");

        ItemStack crafted = event.getCrafting();
        if (!crafted.has(net.minecraft.core.component.DataComponents.FOOD)) return;

        if (serverPlayer.getRandom().nextFloat() >= (enhanced ? 0.50f : 0.25f)) return;

        // Add 1 extra to the stack
        crafted.grow(1);

        ServerLevel level = (ServerLevel) serverPlayer.level();
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.GENERIC_EAT.value(),
                SoundSource.PLAYERS, 0.6f, 1.3f);
        serverPlayer.sendSystemMessage(Component.literal("Master Chef: Extra serving!")
                .withStyle(ChatFormatting.GOLD));
    }

    // ==================== Treasure Sense (fisherman_4) ====================

    /**
     * On fish caught: if player has this node, chance to drop extra treasure items.
     * 5% chance for a "sunken treasure" bonus of 2-3 valuable random items.
     */
    @SubscribeEvent
    public static void onFishTreasureSense(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (!CapstoneManager.hasCapstoneTrigger(serverPlayer, "fisherman_4")) return;
        boolean enhanced = CapstoneManager.hasCapstoneTrigger(serverPlayer, "fisherman_5");

        ServerLevel level = (ServerLevel) serverPlayer.level();

        // Check if catch includes non-fish items (treasure)
        boolean isTreasure = false;
        for (ItemStack drop : event.getDrops()) {
            if (drop.getItem() != Items.COD && drop.getItem() != Items.SALMON
                    && drop.getItem() != Items.TROPICAL_FISH && drop.getItem() != Items.PUFFERFISH) {
                isTreasure = true;
                break;
            }
        }

        // If treasure catch, spawn an extra treasure item
        if (isTreasure) {
            ItemStack bonusTreasure = getRandomTreasureItem(serverPlayer);
            ItemEntity itemEntity = new ItemEntity(level,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), bonusTreasure);
            level.addFreshEntity(itemEntity);
        }

        // 5% (or 15% if enhanced) chance for sunken treasure bonus
        if (serverPlayer.getRandom().nextFloat() < (enhanced ? 0.15f : 0.05f)) {
            int bonusCount = 2 + serverPlayer.getRandom().nextInt(2); // 2-3 items
            for (int i = 0; i < bonusCount; i++) {
                ItemStack loot = getRandomSunkenTreasure(serverPlayer);
                ItemEntity itemEntity = new ItemEntity(level,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), loot);
                level.addFreshEntity(itemEntity);
            }

            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.PLAYERS, 0.8f, 1.0f);
            serverPlayer.sendSystemMessage(Component.literal("Treasure Sense: Sunken treasure found!")
                    .withStyle(ChatFormatting.GOLD));
            level.sendParticles(ParticleTypes.NAUTILUS, serverPlayer.getX(), serverPlayer.getY() + 0.5,
                    serverPlayer.getZ(), 15, 1.0, 0.5, 1.0, 0.05);
        }
    }

    private static ItemStack getRandomTreasureItem(ServerPlayer player) {
        int roll = player.getRandom().nextInt(6);
        return switch (roll) {
            case 0 -> new ItemStack(Items.IRON_INGOT, 1 + player.getRandom().nextInt(3));
            case 1 -> new ItemStack(Items.GOLD_INGOT, 1 + player.getRandom().nextInt(2));
            case 2 -> new ItemStack(Items.NAME_TAG);
            case 3 -> new ItemStack(Items.NAUTILUS_SHELL);
            case 4 -> new ItemStack(Items.SADDLE);
            case 5 -> new ItemStack(Items.ENCHANTED_BOOK);
            default -> new ItemStack(Items.IRON_INGOT);
        };
    }

    private static ItemStack getRandomSunkenTreasure(ServerPlayer player) {
        int roll = player.getRandom().nextInt(8);
        return switch (roll) {
            case 0 -> new ItemStack(Items.DIAMOND);
            case 1 -> new ItemStack(Items.EMERALD, 1 + player.getRandom().nextInt(3));
            case 2 -> new ItemStack(Items.GOLD_INGOT, 2 + player.getRandom().nextInt(4));
            case 3 -> new ItemStack(Items.IRON_INGOT, 3 + player.getRandom().nextInt(5));
            case 4 -> new ItemStack(Items.NAUTILUS_SHELL);
            case 5 -> new ItemStack(Items.ENDER_PEARL, 1 + player.getRandom().nextInt(2));
            case 6 -> new ItemStack(Items.NAME_TAG);
            case 7 -> new ItemStack(Items.HEART_OF_THE_SEA);
            default -> new ItemStack(Items.GOLD_INGOT);
        };
    }
}
