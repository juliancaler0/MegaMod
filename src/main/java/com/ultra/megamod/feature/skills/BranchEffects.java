package com.ultra.megamod.feature.skills;

import com.ultra.megamod.feature.skills.capstone.CapstoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gameplay effects for branches that otherwise only give passive XP bonuses.
 * These make Ore Finder, Crop Master, Enchanter, Explorer, Shield Wall,
 * Tunnel Rat, Smelter, Animal Handler, Botanist, Cook, Fisherman,
 * Hunter Instinct, and Navigator feel more interesting at tiers 2-3
 * even before the tier 4 capstone.
 */
@EventBusSubscriber(modid = "megamod")
public class BranchEffects {

    // Navigator T3 sprint tracking
    private static final Map<UUID, Long> SPRINT_START = new HashMap<>();

    // Explorer reach modifier ID
    private static final Identifier EXPLORER_REACH_ID = Identifier.fromNamespaceAndPath("megamod", "explorer_reach");

    // Relic Lore T3 magic shield cooldown
    private static final Map<UUID, Long> MAGIC_SHIELD_CD = new HashMap<>();

    // ==================== Ore Finder: Ore Shimmer ====================
    // Tier 2+ (ore_finder_2): When mining ore, nearby ores within 4 blocks
    // produce particles, hinting at their location.

    @SubscribeEvent
    public static void onOreBreakShimmer(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockState state = event.getState();
        if (!isOre(state)) return;

        // Requires ore_finder_2+
        if (!CapstoneManager.hasCapstoneTrigger(player, "ore_finder_2")) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos center = event.getPos();
        // vein_sense attribute extends the base shimmer radius
        double veinSense = com.ultra.megamod.feature.attributes.AttributeHelper.getValue(
            (LivingEntity) player, com.ultra.megamod.feature.attributes.MegaModAttributes.VEIN_SENSE);
        int radius = 4 + (int) veinSense;

        // Spawn particles at nearby ore positions and play chime sound
        boolean foundOre = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    BlockPos check = center.offset(dx, dy, dz);
                    if (isOre(serverLevel.getBlockState(check))) {
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                                check.getX() + 0.5, check.getY() + 0.5, check.getZ() + 0.5,
                                3, 0.2, 0.2, 0.2, 0.01);
                        foundOre = true;
                    }
                }
            }
        }
        // Chime sound when nearby ores are detected
        if (foundOre) {
            serverLevel.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.6f, 1.4f);
        }
    }

    // ==================== Ore Finder: Auto-Pickup (Tier 3) ====================
    // ore_finder_3+: Magnet effect - nearby item entities move toward player
    // Handled via tick check

    @SubscribeEvent
    public static void onTickAutoPickup(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 10L != 0L) return; // Every 0.5 seconds

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "ore_finder_3")) continue;

            ServerLevel level = (ServerLevel) player.level();
            // Only while underground (below sea level)
            if (player.blockPosition().getY() > 63) continue;

            // Pull nearby items within 5 blocks
            var items = level.getEntitiesOfClass(
                    net.minecraft.world.entity.item.ItemEntity.class,
                    player.getBoundingBox().inflate(5.0),
                    item -> item.isAlive() && !item.hasPickUpDelay());

            for (var item : items) {
                double dx = player.getX() - item.getX();
                double dy = player.getY() + 0.5 - item.getY();
                double dz = player.getZ() - item.getZ();
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > 0.5) {
                    double speed = 0.15;
                    item.setDeltaMovement(
                            dx / dist * speed,
                            dy / dist * speed + 0.03,
                            dz / dist * speed);
                }
            }
        }
    }

    // ==================== Crop Master: Auto-Replant (Tier 2) ====================
    // crop_master_2+: When harvesting a mature crop, auto-replant it

    @SubscribeEvent
    public static void onCropBreakAutoReplant(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        BlockState state = event.getState();
        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;
        if (!cropBlock.isMaxAge(state)) return; // Only mature crops

        if (!CapstoneManager.hasCapstoneTrigger(player, "crop_master_2")) return;

        // Schedule replant for next tick (after the block is broken)
        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos pos = event.getPos();
        BlockState defaultState = cropBlock.defaultBlockState();

        serverLevel.getServer().execute(() -> {
            // Check if the block is now air (was broken)
            if (serverLevel.getBlockState(pos).isAir()) {
                // Check if farmland is still below
                BlockPos below = pos.below();
                BlockState belowState = serverLevel.getBlockState(below);
                if (belowState.getBlock() == net.minecraft.world.level.block.Blocks.FARMLAND) {
                    serverLevel.setBlock(pos, defaultState, 2);
                    serverLevel.playSound(null, pos, SoundEvents.CROP_BREAK,
                            SoundSource.BLOCKS, 0.5f, 1.2f);
                }
            }
        });
    }

    // ==================== Crop Master: Growth Aura (Tier 3) ====================
    // crop_master_3+: Nearby crops tick faster (up to 3 crops advanced per 10s)

    @SubscribeEvent
    public static void onTickCropGrowthAura(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 200L != 0L) return; // Every 10 seconds

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "crop_master_3")) continue;

            ServerLevel level = (ServerLevel) player.level();
            BlockPos center = player.blockPosition();
            int radius = 5;

            // Nature's Harmony synergy increases number of crops advanced
            float growthMultiplier = com.ultra.megamod.feature.skills.synergy.SynergyEffects.getGrowthSpeedMultiplier(player);
            int maxCrops = (int)(3 * growthMultiplier);
            int ticked = 0;
            for (int dx = -radius; dx <= radius && ticked < maxCrops; dx++) {
                for (int dz = -radius; dz <= radius && ticked < maxCrops; dz++) {
                    for (int dy = -2; dy <= 2 && ticked < maxCrops; dy++) {
                        BlockPos check = center.offset(dx, dy, dz);
                        BlockState checkState = level.getBlockState(check);
                        if (checkState.getBlock() instanceof CropBlock cropBlock) {
                            if (!cropBlock.isMaxAge(checkState)) {
                                // Advance by 1 stage
                                int age = cropBlock.getAge(checkState);
                                level.setBlock(check, cropBlock.getStateForAge(age + 1), 2);
                                level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                        check.getX() + 0.5, check.getY() + 0.5, check.getZ() + 0.5,
                                        2, 0.2, 0.2, 0.2, 0.01);
                                ticked++;
                            }
                        }
                    }
                }
            }
        }
    }

    // ==================== Tunnel Rat: Cave Night Vision (Tier 3) ====================
    // tunnel_rat_3+: Automatically get Night Vision when deep underground

    @SubscribeEvent
    public static void onTickCaveNightVision(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 60L != 0L) return; // Every 3 seconds

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_3")) continue;

            // Must be underground (below Y=40 and can't see sky)
            ServerLevel level = (ServerLevel) player.level();
            if (player.blockPosition().getY() > 40) continue;
            if (level.canSeeSky(player.blockPosition())) continue;

            // Grant Night Vision for 80 ticks (4s, well past next check at 3s)
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 80, 0, false, false, true));
        }
    }

    // ==================== Explorer: Endurance (Tier 2) ====================
    // explorer_2+: Reduced hunger drain while exploring (moving through new chunks).
    // Reduces exhaustion every 100 ticks while the player is moving.

    @SubscribeEvent
    public static void onTickExplorerEndurance(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "explorer_2")) continue;

            // If player is moving (sprinting), reduce exhaustion slightly
            if (player.isSprinting()) {
                player.getFoodData().addExhaustion(-0.5f);
            }
        }
    }

    // ==================== Enchanter: Luck Aura (Tier 3) ====================
    // enchanter_3+: Grant Luck I while near an enchanting table, with ambient particles

    @SubscribeEvent
    public static void onTickEnchanterAura(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!CapstoneManager.hasCapstoneTrigger(player, "enchanter_3")) continue;

            // Grant Luck I while near an enchanting table (5 block radius)
            ServerLevel level = (ServerLevel) player.level();
            BlockPos center = player.blockPosition();
            boolean nearEnchantTable = false;
            for (int dx = -5; dx <= 5 && !nearEnchantTable; dx++) {
                for (int dy = -2; dy <= 2 && !nearEnchantTable; dy++) {
                    for (int dz = -5; dz <= 5 && !nearEnchantTable; dz++) {
                        BlockPos check = center.offset(dx, dy, dz);
                        if (level.getBlockState(check).getBlock() == net.minecraft.world.level.block.Blocks.ENCHANTING_TABLE) {
                            nearEnchantTable = true;
                        }
                    }
                }
            }

            if (nearEnchantTable) {
                player.addEffect(new MobEffectInstance(MobEffects.LUCK, 120, 0, false, false, true));
                // Ambient enchant particles around player
                level.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        5, 0.5, 0.5, 0.5, 0.5);
            }
        }
    }

    // ==================== Shield Wall T2: Counter-Strike ====================
    // shield_wall_2+: Successful shield block grants Strength I for 1 second

    @SubscribeEvent
    public static void onBlockCounterStrike(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getBlocked()) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "shield_wall_2")) return;
        player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 20, 0, false, true, true));
    }

    // ==================== Tunnel Rat T2: Steady Miner ====================
    // tunnel_rat_2+: Cancel knockback while sneaking

    @SubscribeEvent
    public static void onKnockbackSteadyMiner(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.isShiftKeyDown()) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_2")) return;
        event.setCanceled(true);
    }

    // ==================== Animal Handler T2: Protected Pets ====================
    // animal_handler_2+: Tamed animals near owner take 25% less damage

    @SubscribeEvent
    public static void onPetDamageReduction(LivingDamageEvent.Pre event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) return;
        if (!(target instanceof TamableAnimal tamed)) return;
        if (!tamed.isTame()) return;
        LivingEntity owner = tamed.getOwner();
        if (!(owner instanceof ServerPlayer player)) return;
        if (tamed.distanceTo(player) > 10.0) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "animal_handler_2")) return;
        // Base 25% reduction; beast_affinity adds extra reduction (capped at 60% total)
        double beastAffinity = com.ultra.megamod.feature.attributes.AttributeHelper.getValue(
            (LivingEntity) player, com.ultra.megamod.feature.attributes.MegaModAttributes.BEAST_AFFINITY);
        double reductionPct = Math.min(60.0, 25.0 + beastAffinity);
        event.setNewDamage(event.getOriginalDamage() * (float)(1.0 - reductionPct / 100.0));
    }

    // ==================== Animal Handler T3: Pack Hunter ====================
    // animal_handler_3+: Killing a mob grants Speed II to nearby tamed animals

    @SubscribeEvent
    public static void onKillPackHunter(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "animal_handler_3")) return;
        ServerLevel level = (ServerLevel) player.level();
        var pets = level.getEntitiesOfClass(TamableAnimal.class,
                player.getBoundingBox().inflate(10.0),
                a -> a.isTame() && a.isOwnedBy(player));
        for (var pet : pets) {
            pet.addEffect(new MobEffectInstance(MobEffects.SPEED, 100, 1, false, true, true));
        }
    }

    // ==================== Botanist T2: Cross-Pollination ====================
    // botanist_2+: Breaking mature crop has 20% chance to drop a random seed

    @SubscribeEvent
    public static void onCropBreakCrossPollination(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        BlockState state = event.getState();
        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;
        if (!cropBlock.isMaxAge(state)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "botanist_2")) return;
        if (player.getRandom().nextFloat() >= 0.20f) return;
        ServerLevel serverLevel = (ServerLevel) player.level();
        ItemStack seed = getRandomSeed(player);
        ItemEntity itemEntity = new ItemEntity(
                serverLevel, event.getPos().getX() + 0.5, event.getPos().getY() + 0.5,
                event.getPos().getZ() + 0.5, seed);
        serverLevel.addFreshEntity(itemEntity);
    }

    // ==================== Botanist T3: Nourishing Food ====================
    // botanist_3+: Eating food grants Regeneration I for 3 seconds

    @SubscribeEvent
    public static void onEatNourishing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "botanist_3")) return;
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, false, true, true));
    }

    // ==================== Cook T2: Extra Nourishment ====================
    // cook_2+: Eating food restores 1 extra hunger point

    @SubscribeEvent
    public static void onEatExtraNourishment(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "cook_2")) return;
        player.getFoodData().eat(1, 0.3f);
    }

    // ==================== Cook T3: Food Shield ====================
    // cook_3+: Eating food grants Absorption I for 3 seconds

    @SubscribeEvent
    public static void onEatFoodShield(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getItem().has(net.minecraft.core.component.DataComponents.FOOD)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "cook_3")) return;
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, false, true, true));
    }

    // ==================== Fisherman T2: Storm Fisher ====================
    // fisherman_2+: Double fishing drops when it's raining

    @SubscribeEvent
    public static void onFishStormFisher(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "fisherman_2")) return;
        ServerLevel level = (ServerLevel) player.level();
        if (!level.isRaining()) return;
        // Double drops by spawning copies
        for (ItemStack drop : event.getDrops()) {
            ItemEntity bonus = new ItemEntity(
                    level, player.getX(), player.getY(), player.getZ(), drop.copy());
            level.addFreshEntity(bonus);
        }
    }

    // ==================== Hunter Instinct T3: Kill Momentum ====================
    // hunter_instinct_3+: Killing a non-player mob grants Speed I for 2 seconds

    @SubscribeEvent
    public static void onKillMomentum(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        if (event.getEntity() instanceof Player) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "hunter_instinct_3")) return;
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 0, false, true, true));
    }

    // ==================== Tick-Based Effects (every 20 ticks) ====================
    // Navigator T2: Terrain Master, Navigator T3: Parkour Momentum

    @SubscribeEvent
    public static void onTickEvery20(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 20L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) player.level();

            // --- Navigator T2: Terrain Master ---
            // Water: Dolphin's Grace, Soul Sand/Soil: Speed I
            if (CapstoneManager.hasCapstoneTrigger(player, "navigator_2")) {
                if (player.isInWater()) {
                    player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 30, 0, false, false, true));
                }
                BlockPos below = player.blockPosition().below();
                Block blockBelow = level.getBlockState(below).getBlock();
                if (blockBelow == Blocks.SOUL_SAND || blockBelow == Blocks.SOUL_SOIL) {
                    player.addEffect(new MobEffectInstance(MobEffects.SPEED, 30, 0, false, false, true));
                }
            }

            // --- Navigator T3: Parkour Momentum ---
            // Sprinting for 5+ seconds grants Jump Boost I
            if (CapstoneManager.hasCapstoneTrigger(player, "navigator_3")) {
                UUID uid = player.getUUID();
                if (player.isSprinting()) {
                    long startTick = SPRINT_START.getOrDefault(uid, gameTime);
                    if (!SPRINT_START.containsKey(uid)) SPRINT_START.put(uid, gameTime);
                    if (gameTime - startTick > 100) {
                        player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 30, 0, false, false, true));
                    }
                } else {
                    SPRINT_START.remove(uid);
                }
            }

            // --- Berserker T2/T3: Low-HP Strength ---
            // Below 50% HP: Strength I (T2) or Strength II (T3)
            if (CapstoneManager.hasCapstoneTrigger(player, "berserker_2")) {
                if (player.getHealth() < player.getMaxHealth() * 0.5f) {
                    boolean bersT3 = CapstoneManager.hasCapstoneTrigger(player, "berserker_3");
                    int bersAmp = bersT3 ? 1 : 0;
                    player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 30, bersAmp, false, false, true));
                }
            }

            // --- Spell Blade T3: Elemental Weapon Shimmer ---
            // Ambient elemental particles while holding a weapon
            if (CapstoneManager.hasCapstoneTrigger(player, "spell_blade_3")) {
                ItemStack mainHand = player.getMainHandItem();
                if (!mainHand.isEmpty() && mainHand.isDamageableItem()) {
                    level.sendParticles(ParticleTypes.ENCHANT,
                            player.getX(), player.getY() + 1.2, player.getZ(),
                            2, 0.2, 0.3, 0.2, 0.3);
                }
            }
        }
    }

    // ==================== Tick-Based Effects (every 60 ticks) ====================
    // Tunnel Rat T3: Dark Mining

    @SubscribeEvent
    public static void onTickEvery60(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 60L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) player.level();

            // --- Tunnel Rat T3: Dark Mining ---
            // Below Y=40 in low light: Haste I for 80 ticks
            if (CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_3")) {
                if (player.blockPosition().getY() <= 40) {
                    int light = level.getMaxLocalRawBrightness(player.blockPosition());
                    if (light < 7) {
                        player.addEffect(new MobEffectInstance(MobEffects.HASTE, 80, 0, false, false, true));
                    }
                }
            }
        }
    }

    // ==================== Tick-Based Effects (every 100 ticks) ====================
    // Smelter T2: Faster Furnace, Fisherman T3: Ocean Power

    @SubscribeEvent
    public static void onTickEvery100(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 100L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) player.level();

            // --- Smelter T2/T3: Actual Faster Furnaces ---
            // Tick nearby furnaces/blast furnaces extra times (T2: +1 tick, T3: +2 ticks)
            if (CapstoneManager.hasCapstoneTrigger(player, "smelter_2")) {
                int extraTicks = CapstoneManager.hasCapstoneTrigger(player, "smelter_3") ? 2 : 1;
                BlockPos center = player.blockPosition();
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        for (int dz = -5; dz <= 5; dz++) {
                            BlockPos pos = center.offset(dx, dy, dz);
                            Block b = level.getBlockState(pos).getBlock();
                            if (b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE) {
                                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace) {
                                    for (int t = 0; t < extraTicks; t++) {
                                        net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Fisherman T3: Ocean Power ---
            // Conduit Power while in water in ocean biome
            if (CapstoneManager.hasCapstoneTrigger(player, "fisherman_3")) {
                if (player.isInWater()) {
                    String biomeName = level.getBiome(player.blockPosition()).unwrapKey()
                            .map(k -> k.identifier().getPath()).orElse("");
                    if (biomeName.contains("ocean")) {
                        player.addEffect(new MobEffectInstance(MobEffects.CONDUIT_POWER, 120, 0, false, false, true));
                    }
                }
            }

            // --- Cook T2/T3: Actual Faster Smokers ---
            // Tick nearby smokers extra times (T2: +1 tick, T3: +2 ticks)
            if (CapstoneManager.hasCapstoneTrigger(player, "cook_2")) {
                int cookExtraTicks = CapstoneManager.hasCapstoneTrigger(player, "cook_3") ? 2 : 1;
                BlockPos cookCenter = player.blockPosition();
                for (int dx = -5; dx <= 5; dx++) {
                    for (int dy = -3; dy <= 3; dy++) {
                        for (int dz = -5; dz <= 5; dz++) {
                            BlockPos pos = cookCenter.offset(dx, dy, dz);
                            if (level.getBlockState(pos).getBlock() == Blocks.SMOKER) {
                                net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
                                if (be instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity furnace) {
                                    for (int t = 0; t < cookExtraTicks; t++) {
                                        net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity.serverTick(level, pos, level.getBlockState(pos), furnace);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Endurance T2/T3: Slower Hunger ---
            // Reduce exhaustion periodically
            if (CapstoneManager.hasCapstoneTrigger(player, "endurance_2")) {
                boolean endT3 = CapstoneManager.hasCapstoneTrigger(player, "endurance_3");
                float reduction = endT3 ? -0.8f : -0.4f;
                player.getFoodData().addExhaustion(reduction);
            }

            // --- Explorer T2/T3: Increased Reach ---
            // Add reach bonus via attribute modifiers
            if (CapstoneManager.hasCapstoneTrigger(player, "explorer_2")) {
                boolean expT3 = CapstoneManager.hasCapstoneTrigger(player, "explorer_3");
                double reachBonus = expT3 ? 1.0 : 0.5;
                applyExplorerReach(player, reachBonus);
            }

            // --- Summoner T2: Pet Regeneration ---
            // Tamed animals within 8 blocks slowly regenerate health
            if (CapstoneManager.hasCapstoneTrigger(player, "summoner_2")) {
                java.util.List<TamableAnimal> pets = level.getEntitiesOfClass(TamableAnimal.class,
                        player.getBoundingBox().inflate(8.0),
                        pet -> pet.isAlive() && pet.isTame() && pet.isOwnedBy(player)
                                && pet.getHealth() < pet.getMaxHealth());
                for (TamableAnimal pet : pets) {
                    pet.heal(1.0f);
                    level.sendParticles(ParticleTypes.HEART,
                            pet.getX(), pet.getY() + 0.8, pet.getZ(),
                            1, 0.2, 0.2, 0.2, 0.0);
                }
            }
        }
    }

    // ==================== Tick-Based Effects (every 600 ticks) ====================
    // Hunter Instinct T2: Predator Sense

    @SubscribeEvent
    public static void onTickEvery600(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 600L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            ServerLevel level = (ServerLevel) player.level();

            // --- Hunter Instinct T2: Predator Sense ---
            // Apply Glowing to hostile mobs; prey_sense attribute extends base range
            if (CapstoneManager.hasCapstoneTrigger(player, "hunter_instinct_2")) {
                double preySense = com.ultra.megamod.feature.attributes.AttributeHelper.getValue(
                    (LivingEntity) player, com.ultra.megamod.feature.attributes.MegaModAttributes.PREY_SENSE);
                double range = 16.0 + preySense;
                var hostiles = level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,
                        player.getBoundingBox().inflate(range),
                        m -> m.isAlive() && m instanceof net.minecraft.world.entity.monster.Monster);
                for (var mob : hostiles) {
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false, false));
                }
            }
        }
    }

    // ==================== Tick-Based Effects (every 5 ticks) ====================
    // Tunnel Rat T2/T3: Faster Climbing

    @SubscribeEvent
    public static void onTickEvery5(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 5L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // --- Tunnel Rat T2/T3: Faster Climbing ---
            // Boost Y motion while on ladders/vines
            if (!CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_2")) continue;
            if (!player.onClimbable()) continue;
            boolean trT3 = CapstoneManager.hasCapstoneTrigger(player, "tunnel_rat_3");
            double climbBoost = trT3 ? 0.08 : 0.04;
            player.setDeltaMovement(player.getDeltaMovement().add(0, climbBoost, 0));
        }
    }

    // ==================== Tick-Based Effects (every 10 ticks) ====================
    // Navigator T2/T3: Faster Boats

    @SubscribeEvent
    public static void onTickEvery10(ServerTickEvent.Post event) {
        long gameTime = event.getServer().overworld().getGameTime();
        if (gameTime % 10L != 0L) return;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            // --- Navigator T2/T3: Faster Boats ---
            if (!player.isPassenger()) continue;
            Entity vehicle = player.getVehicle();
            if (vehicle == null) continue;
            String vehicleType = BuiltInRegistries.ENTITY_TYPE.getKey(vehicle.getType()).getPath();
            if (!vehicleType.contains("boat") && !vehicleType.contains("raft")) continue;
            if (!CapstoneManager.hasCapstoneTrigger(player, "navigator_2")) continue;
            boolean navT3 = CapstoneManager.hasCapstoneTrigger(player, "navigator_3");
            double boatBoost = navT3 ? 0.03 : 0.015;
            double yaw = Math.toRadians(vehicle.getYRot());
            vehicle.setDeltaMovement(vehicle.getDeltaMovement().add(
                    -Math.sin(yaw) * boatBoost, 0, Math.cos(yaw) * boatBoost));
        }
    }

    // ==================== Fisherman T3: Bonus Fishing XP ====================
    // fisherman_3+: Fishing grants extra XP orbs

    @SubscribeEvent
    public static void onFishXpBonus(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "fisherman_3")) return;
        ServerLevel level = (ServerLevel) player.level();
        net.minecraft.world.entity.ExperienceOrb.award(level, player.position(), 3 + player.getRandom().nextInt(4));
    }

    // ==================== Efficient Mining T2/T3: Durability Savings ====================
    // 15% (T2) or 30% (T3) chance to not consume pickaxe/shovel durability on block break

    @SubscribeEvent
    public static void onMineDurabilitySave(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        ItemStack tool = player.getMainHandItem();
        if (tool.isEmpty() || !tool.isDamageableItem()) return;
        String itemPath = BuiltInRegistries.ITEM.getKey(tool.getItem()).getPath();
        if (!itemPath.contains("pickaxe") && !itemPath.contains("shovel")) return;
        boolean emT3 = CapstoneManager.hasCapstoneTrigger(player, "efficient_mining_3");
        boolean emT2 = emT3 || CapstoneManager.hasCapstoneTrigger(player, "efficient_mining_2");
        if (!emT2) return;
        float chance = emT3 ? 0.30f : 0.15f;
        if (player.getRandom().nextFloat() < chance) {
            tool.setDamageValue(Math.max(0, tool.getDamageValue() - 1));
        }
    }

    // ==================== Gem Cutter T3: Double Ore Drops ====================
    // 5% chance any ore drops double items

    @SubscribeEvent
    public static void onOreDoubleDrops(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "gem_cutter_3")) return;
        BlockState state = event.getState();
        if (!state.is(net.minecraft.tags.BlockTags.GOLD_ORES) && !state.is(net.minecraft.tags.BlockTags.IRON_ORES)
                && !state.is(net.minecraft.tags.BlockTags.COAL_ORES) && !state.is(net.minecraft.tags.BlockTags.DIAMOND_ORES)
                && !state.is(net.minecraft.tags.BlockTags.EMERALD_ORES) && !state.is(net.minecraft.tags.BlockTags.LAPIS_ORES)
                && !state.is(net.minecraft.tags.BlockTags.REDSTONE_ORES) && !state.is(net.minecraft.tags.BlockTags.COPPER_ORES)) {
            return;
        }
        if (player.getRandom().nextFloat() < 0.05f) {
            ServerLevel serverLevel = (ServerLevel) player.level();
            java.util.List<net.minecraft.world.item.ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(
                    state, serverLevel, event.getPos(), null, player, player.getMainHandItem());
            for (net.minecraft.world.item.ItemStack drop : drops) {
                net.minecraft.world.level.block.Block.popResource(serverLevel, event.getPos(), drop.copy());
            }
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Double ore drop!").withStyle(net.minecraft.ChatFormatting.AQUA));
        }
    }

    // ==================== Ranged Precision T2/T3: Post-Shot Speed ====================
    // Brief Speed I after releasing a bow for repositioning (T2: 20 ticks, T3: 40 ticks)

    @SubscribeEvent
    public static void onArrowFireSpeed(LivingEntityUseItemEvent.Stop event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        String item = BuiltInRegistries.ITEM.getKey(event.getItem().getItem()).getPath();
        if (!item.contains("bow")) return;
        boolean rpT3 = CapstoneManager.hasCapstoneTrigger(player, "ranged_precision_3");
        boolean rpT2 = rpT3 || CapstoneManager.hasCapstoneTrigger(player, "ranged_precision_2");
        if (!rpT2) return;
        int duration = rpT3 ? 40 : 20;
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, duration, 0, false, false, true));
    }

    // ==================== Shield Wall T2/T3: Durability Save ====================
    // 20% (T2) or 40% (T3) chance shield doesn't lose durability on block

    @SubscribeEvent
    public static void onShieldDurabilitySave(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!event.getBlocked()) return;
        boolean swT3 = CapstoneManager.hasCapstoneTrigger(player, "shield_wall_3");
        boolean swT2 = swT3 || CapstoneManager.hasCapstoneTrigger(player, "shield_wall_2");
        if (!swT2) return;
        float chance = swT3 ? 0.40f : 0.20f;
        if (player.getRandom().nextFloat() < chance) {
            ItemStack shield = player.getUseItem();
            if (!shield.isEmpty() && shield.isDamageableItem()) {
                shield.setDamageValue(Math.max(0, shield.getDamageValue() - 1));
            }
        }
    }

    // ==================== Blade Mastery T2/T3: Sword Durability Save ====================
    // 15% (T2) or 30% (T3) chance to repair sword by 1 after dealing damage

    @SubscribeEvent
    public static void onSwordDurabilitySave(LivingDamageEvent.Post event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isDamageableItem()) return;
        String name = BuiltInRegistries.ITEM.getKey(weapon.getItem()).getPath();
        if (!name.contains("sword")) return;
        boolean bmT3 = CapstoneManager.hasCapstoneTrigger(player, "blade_mastery_3");
        boolean bmT2 = bmT3 || CapstoneManager.hasCapstoneTrigger(player, "blade_mastery_2");
        if (!bmT2) return;
        float chance = bmT3 ? 0.30f : 0.15f;
        if (player.getRandom().nextFloat() < chance) {
            weapon.setDamageValue(Math.max(0, weapon.getDamageValue() - 1));
        }
    }

    // ==================== Animal Handler T2/T3: Extra Animal Drops ====================
    // 1 (T2) or 2 (T3) extra drops when killing animals

    @SubscribeEvent
    public static void onAnimalExtraDrops(LivingDeathEvent event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        LivingEntity killed = event.getEntity();
        if (killed instanceof Player) return;
        if (!(killed instanceof Animal)) return;
        boolean ahT3 = CapstoneManager.hasCapstoneTrigger(player, "animal_handler_3");
        boolean ahT2 = ahT3 || CapstoneManager.hasCapstoneTrigger(player, "animal_handler_2");
        if (!ahT2) return;
        ServerLevel level = (ServerLevel) player.level();
        int extraDrops = ahT3 ? 2 : 1;
        for (int i = 0; i < extraDrops; i++) {
            ItemStack drop = getAnimalDrop(killed);
            if (!drop.isEmpty()) {
                level.addFreshEntity(new ItemEntity(
                        level, killed.getX(), killed.getY() + 0.5, killed.getZ(), drop));
            }
        }
    }

    // ==================== Crop Master T2/T3: Bone Meal Efficiency ====================
    // Bone meal advances crops by 1 (T2) or 2 (T3) extra growth stages

    @SubscribeEvent
    public static void onBoneMealEfficiency(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (stack.getItem() != Items.BONE_MEAL) return;
        Level level = event.getLevel();
        if (level.isClientSide()) return;
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock cropBlock)) return;
        boolean cmT3 = CapstoneManager.hasCapstoneTrigger(player, "crop_master_3");
        boolean cmT2 = cmT3 || CapstoneManager.hasCapstoneTrigger(player, "crop_master_2");
        if (!cmT2) return;
        int extra = cmT3 ? 2 : 1;
        int currentAge = cropBlock.getAge(state);
        int maxAge = cropBlock.getMaxAge();
        int newAge = Math.min(maxAge, currentAge + extra);
        if (newAge > currentAge) {
            ((ServerLevel) level).setBlock(pos, cropBlock.getStateForAge(newAge), 2);
        }
    }

    // ==================== Tactician T2/T3: Crit Durability Save ====================
    // 20% (T2) or 40% (T3) chance to repair weapon by 1 on critical hit

    @SubscribeEvent
    public static void onCritDurabilitySave(LivingDamageEvent.Post event) {
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;
        // Basic crit check: player is falling and not on ground
        if (!(player.fallDistance > 0.0f && !player.onGround())) return;
        ItemStack weapon = player.getMainHandItem();
        if (weapon.isEmpty() || !weapon.isDamageableItem()) return;
        boolean tacT3 = CapstoneManager.hasCapstoneTrigger(player, "tactician_3");
        boolean tacT2 = tacT3 || CapstoneManager.hasCapstoneTrigger(player, "tactician_2");
        if (!tacT2) return;
        float chance = tacT3 ? 0.40f : 0.20f;
        if (player.getRandom().nextFloat() < chance) {
            weapon.setDamageValue(Math.max(0, weapon.getDamageValue() - 1));
        }
    }

    // ==================== Cleanup ====================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        SPRINT_START.remove(uuid);
    }

    // ==================== Utility ====================

    private static boolean isOre(BlockState state) {
        return state.is(BlockTags.COAL_ORES) || state.is(BlockTags.IRON_ORES)
                || state.is(BlockTags.GOLD_ORES) || state.is(BlockTags.DIAMOND_ORES)
                || state.is(BlockTags.EMERALD_ORES) || state.is(BlockTags.LAPIS_ORES)
                || state.is(BlockTags.REDSTONE_ORES) || state.is(BlockTags.COPPER_ORES);
    }

    private static ItemStack getRandomSeed(ServerPlayer player) {
        int roll = player.getRandom().nextInt(6);
        return switch (roll) {
            case 0 -> new ItemStack(Items.WHEAT_SEEDS);
            case 1 -> new ItemStack(Items.BEETROOT_SEEDS);
            case 2 -> new ItemStack(Items.MELON_SEEDS);
            case 3 -> new ItemStack(Items.PUMPKIN_SEEDS);
            case 4 -> new ItemStack(Items.CARROT);
            case 5 -> new ItemStack(Items.POTATO);
            default -> new ItemStack(Items.WHEAT_SEEDS);
        };
    }

    private static ItemStack getAnimalDrop(LivingEntity animal) {
        String type = BuiltInRegistries.ENTITY_TYPE.getKey(animal.getType()).getPath();
        return switch (type) {
            case "cow", "mooshroom" -> new ItemStack(Items.LEATHER);
            case "pig" -> new ItemStack(Items.PORKCHOP);
            case "chicken" -> new ItemStack(Items.CHICKEN);
            case "sheep" -> new ItemStack(Items.MUTTON);
            case "rabbit" -> new ItemStack(Items.RABBIT);
            case "cod" -> new ItemStack(Items.COD);
            case "salmon" -> new ItemStack(Items.SALMON);
            default -> ItemStack.EMPTY;
        };
    }

    // ==================== Relic Lore T2: Arcane XP Drops on Kill ====================
    // relic_lore_2+: 15% chance on mob kill to drop bonus arcane XP.

    @SubscribeEvent
    public static void onKillArcaneXpDrop(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "relic_lore_2")) return;

        if (player.getRandom().nextFloat() >= 0.15f) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        LivingEntity victim = event.getEntity();

        // Bonus arcane XP
        com.ultra.megamod.feature.skills.SkillManager.get(serverLevel.getServer().overworld())
                .addXp(player.getUUID(), com.ultra.megamod.feature.skills.SkillTreeType.ARCANE, 3);
        serverLevel.sendParticles(ParticleTypes.ENCHANT,
                victim.getX(), victim.getY() + 1.0, victim.getZ(),
                8, 0.3, 0.5, 0.3, 0.5);
    }

    // ==================== Relic Lore T3: Magic Shield when Low HP ====================
    // relic_lore_3+: Taking damage below 30% HP grants Absorption I for 3s (60s CD).

    @SubscribeEvent
    public static void onDamageMagicShield(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "relic_lore_3")) return;

        float healthAfter = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (healthAfter > maxHealth * 0.30f) return;

        ServerLevel level = (ServerLevel) player.level();
        long currentTick = level.getGameTime();
        Long lastUsed = MAGIC_SHIELD_CD.get(player.getUUID());
        if (lastUsed != null && currentTick - lastUsed < 1200L) return; // 60s CD
        MAGIC_SHIELD_CD.put(player.getUUID(), currentTick);

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60, 0, false, true, true));
        level.sendParticles(ParticleTypes.ENCHANTED_HIT,
                player.getX(), player.getY() + 1.0, player.getZ(),
                12, 0.5, 0.8, 0.5, 0.1);
        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.7f, 1.5f);
    }

    // ==================== Spell Blade T2: Burning Trail ====================
    // spell_blade_2+: 10% chance on melee hit to ignite target for 2s.

    @SubscribeEvent
    public static void onMeleeHitBurningTrail(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "spell_blade_2")) return;
        // Only direct melee
        if (event.getSource().getDirectEntity() != sourceEntity) return;
        if (player.getRandom().nextFloat() >= 0.10f) return;

        event.getEntity().igniteForSeconds(2);
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(ParticleTypes.FLAME,
                event.getEntity().getX(), event.getEntity().getY() + 0.5, event.getEntity().getZ(),
                5, 0.2, 0.3, 0.2, 0.02);
    }

    // ==================== Spell Blade T3: Elemental Weapon Shimmer ====================
    // spell_blade_3+: Ambient particles around player while holding a weapon (every 40 ticks).
    // Handled in the tick handler (onTickEvery20 section) — search for "spell_blade_3" below.

    // ==================== Summoner T2: Pet Regeneration ====================
    // summoner_2+: Tamed animals within 8 blocks slowly regen health (every 100 ticks).
    // Handled in the tick handler (onTickEvery100 section) — search for "summoner_2" below.

    // ==================== Mana Weaver T2: Regen after Relic Ability ====================
    // mana_weaver_2+: Taking magic damage while at 0 hunger saturation grants Regen I for 3s.
    // (Proxy for "after using a relic ability" — magic damage happens during relic interactions.)

    // ==================== Mana Weaver T3: XP orbs restore hunger ====================
    // mana_weaver_3+: Picking up XP orbs restores a tiny bit of hunger.

    @SubscribeEvent
    public static void onXpPickupRestoreHunger(net.neoforged.neoforge.event.entity.player.PlayerXpEvent.PickupXp event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "mana_weaver_3")) return;

        // Restore a small amount of hunger (0.5 per XP orb)
        net.minecraft.world.food.FoodData food = player.getFoodData();
        if (food.getFoodLevel() < 20) {
            food.eat(1, 0.5f);
        }
    }

    // ==================== Gem Cutter T2: Gem Sparkle on Mine ====================
    // gem_cutter_2+: Gem-type ores chime and sparkle with happy villager particles when mined.

    @SubscribeEvent
    public static void onGemOreSparkle(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "gem_cutter_2")) return;

        Block block = event.getState().getBlock();
        if (!isGemOre(block)) return;

        ServerLevel serverLevel = (ServerLevel) player.level();
        BlockPos pos = event.getPos();

        // Sparkle particles at the mined gem ore
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                6, 0.3, 0.3, 0.3, 0.02);
        serverLevel.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                4, 0.2, 0.2, 0.2, 0.03);
        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.8f, 1.6f);
    }

    private static boolean isGemOre(Block block) {
        return block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || block == Blocks.AMETHYST_CLUSTER || block == Blocks.NETHER_QUARTZ_ORE;
    }

    // ==================== Dungeoneer T2/T3: Extra Dungeon Mob Drops ====================
    // dungeoneer_2+: Mobs killed in the dungeon dimension drop extra items.

    @SubscribeEvent
    public static void onDungeonMobDrop(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof ServerPlayer player)) return;
        if (!CapstoneManager.hasCapstoneTrigger(player, "dungeoneer_2")) return;

        // Only applies in the dungeon dimension
        String dim = player.level().dimension().identifier().getPath();
        if (!dim.contains("dungeon")) return;

        boolean tier3 = CapstoneManager.hasCapstoneTrigger(player, "dungeoneer_3");
        double bonusChance = tier3 ? 0.50 : 0.25;

        if (player.getRandom().nextFloat() >= bonusChance) return;

        // Drop 1-2 extra items from the mob's loot table (simplified: drop XP + random loot)
        ServerLevel serverLevel = (ServerLevel) player.level();
        LivingEntity victim = event.getEntity();
        int extraXp = tier3 ? 8 : 4;

        // Spawn extra XP orbs
        net.minecraft.world.entity.ExperienceOrb.award(serverLevel, victim.position(), extraXp);

        // Drop a bonus common dungeon item
        ItemStack bonusDrop = switch (player.getRandom().nextInt(5)) {
            case 0 -> new ItemStack(Items.IRON_INGOT, 1 + player.getRandom().nextInt(2));
            case 1 -> new ItemStack(Items.GOLD_INGOT);
            case 2 -> new ItemStack(Items.ARROW, 2 + player.getRandom().nextInt(4));
            case 3 -> new ItemStack(Items.BREAD, 1 + player.getRandom().nextInt(2));
            default -> new ItemStack(Items.BONE, 1 + player.getRandom().nextInt(3));
        };
        victim.spawnAtLocation(serverLevel, bonusDrop);
    }

    // ==================== Animal Handler T1: Reduced Breeding Cooldown ====================
    // animal_handler_1+: Animals you breed have a shorter breeding cooldown.

    @SubscribeEvent
    public static void onAnimalBreed(net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent event) {
        if (event.getCausedByPlayer() == null) return;
        Player player = event.getCausedByPlayer();
        if (player.level().isClientSide()) return;
        if (!(player instanceof ServerPlayer sp)) return;
        if (!CapstoneManager.hasCapstoneTrigger(sp, "animal_handler_1")) return;

        // Reduce the breeding cooldown of both parents (vanilla = 6000 ticks = 5 min)
        // T1 reduces to ~3 min
        LivingEntity parentA = event.getParentA();
        LivingEntity parentB = event.getParentB();
        int reducedCooldown = 3600; // 3 minutes instead of 5
        if (parentA instanceof Animal animalA) {
            animalA.setAge(reducedCooldown);
        }
        if (parentB instanceof Animal animalB) {
            animalB.setAge(reducedCooldown);
        }
    }

    private static void applyExplorerReach(ServerPlayer player, double reachBonus) {
        var blockRange = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (blockRange != null) {
            blockRange.removeModifier(EXPLORER_REACH_ID);
            blockRange.addPermanentModifier(new AttributeModifier(
                    EXPLORER_REACH_ID, reachBonus, AttributeModifier.Operation.ADD_VALUE));
        }
        var entityRange = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (entityRange != null) {
            entityRange.removeModifier(EXPLORER_REACH_ID);
            entityRange.addPermanentModifier(new AttributeModifier(
                    EXPLORER_REACH_ID, reachBonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
