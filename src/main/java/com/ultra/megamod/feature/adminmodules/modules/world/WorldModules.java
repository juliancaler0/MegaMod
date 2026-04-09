package com.ultra.megamod.feature.adminmodules.modules.world;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class WorldModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new AutoBreed());
        reg.accept(new AutoShearer());
        reg.accept(new Timer());
        reg.accept(new SpawnProofer());
        reg.accept(new Excavator());
        reg.accept(new LiquidFiller());
        reg.accept(new NoGhostBlocks());
        reg.accept(new AutoSmelt());
        reg.accept(new AutoFarm());
        reg.accept(new EChestFarmer());
        reg.accept(new AutoBrush());
        reg.accept(new AirPlace());
        reg.accept(new StashFinder());
        reg.accept(new HighwayBuilder());
        reg.accept(new EndermanLook());
        reg.accept(new AutoMount());
        reg.accept(new Flattener());
        reg.accept(new AutoNametag());
        reg.accept(new AutoWither());
        reg.accept(new AutoSignEditor());
        reg.accept(new EntityCleaner());
        reg.accept(new TreeAura());
        reg.accept(new PacketCanceller());
        // New Meteor-inspired modules
        reg.accept(new Ambience());
        reg.accept(new AutoBrewer());
        reg.accept(new AutoSmelter());
        reg.accept(new BuildHeight());
        reg.accept(new Collisions());
        reg.accept(new Flamethrower());
        reg.accept(new MountBypass());
        // New Meteor-inspired modules (batch 2)
        reg.accept(new InfinityMiner());
    }

    // ======================== EXISTING MODULES (fully implemented) ========================

    static class AutoBreed extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        AutoBreed() { super("auto_breed", "AutoBreed", "Auto-breeds nearby animals", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 8, 4, 16, "Breeding range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            int r = range.getValue();
            List<Animal> animals = level.getEntitiesOfClass(Animal.class, player.getBoundingBox().inflate(r));
            for (Animal a : animals) {
                if (a.canFallInLove() && !a.isInLove()) {
                    a.setInLove(player);
                }
            }
        }
    }

    static class AutoShearer extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        AutoShearer() { super("auto_shearer", "AutoShearer", "Auto-shears nearby sheep", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 8, 4, 16, "Shearing range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            int r = range.getValue();
            List<Sheep> sheep = level.getEntitiesOfClass(Sheep.class, player.getBoundingBox().inflate(r));
            for (Sheep s : sheep) {
                if (!s.isSheared()) {
                    s.shear(level, net.minecraft.sounds.SoundSource.PLAYERS, new ItemStack(Items.SHEARS));
                }
            }
        }
    }

    static class Timer extends AdminModule {
        private ModuleSetting.DoubleSetting speed;
        int tick = 0;
        Timer() { super("timer", "Timer", "Simulates faster gameplay via Speed and Haste effects", ModuleCategory.WORLD); }
        @Override protected void initSettings() { speed = decimal("Speed", 2.0, 0.1, 10.0, "Tick speed multiplier"); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Rate limit: refresh every 30 ticks (effects last 40 ticks)
            if (++tick % 30 != 0) return;
            double s = speed.getValue();
            int speedAmp = Math.max(0, (int) ((s - 1.0) * 2.0));
            int hasteAmp = Math.max(0, (int) ((s - 1.0) * 2.0));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, speedAmp, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HASTE, 40, hasteAmp, false, false));
        }
        @Override public void onDisable(ServerPlayer player) {
            player.removeEffect(MobEffects.SPEED);
            player.removeEffect(MobEffects.HASTE);
        }
    }

    static class SpawnProofer extends AdminModule {
        private ModuleSetting.IntSetting radius;
        int tick = 0;
        SpawnProofer() { super("spawn_proofer", "SpawnProofer", "Places torches on dark blocks", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 8, 2, 16, "Scan radius");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            int r = radius.getValue();
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    for (int y = -2; y <= 2; y++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (level.getBlockState(pos).isAir() && level.getRawBrightness(pos, 0) < 8
                            && level.getBlockState(pos.below()).isSolidRender()) {
                            level.setBlock(pos, Blocks.TORCH.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    static class Excavator extends AdminModule {
        private ModuleSetting.EnumSetting shape;
        int tick = 0;
        Excavator() { super("excavator", "Excavator", "Breaks area around looked-at block", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            shape = enumVal("Shape", "3x3", List.of("3x3", "5x5", "Circle"), "Excavation shape");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;
            HitResult hit = player.pick(5.0, 0.0f, false);
            if (hit.getType() != HitResult.Type.BLOCK) return;
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos center = blockHit.getBlockPos();
            Direction face = blockHit.getDirection();
            String s = shape.getValue();
            int half = "5x5".equals(s) ? 2 : 1;
            int maxBreak = "5x5".equals(s) ? 25 : 9;
            int broken = 0;
            for (int u = -half; u <= half && broken < maxBreak; u++) {
                for (int v = -half; v <= half && broken < maxBreak; v++) {
                    if ("Circle".equals(s) && (u*u + v*v) > half*half) continue;
                    BlockPos target;
                    if (face.getAxis() == Direction.Axis.Y) {
                        target = center.offset(u, 0, v);
                    } else if (face.getAxis() == Direction.Axis.X) {
                        target = center.offset(0, u, v);
                    } else {
                        target = center.offset(u, v, 0);
                    }
                    if (!level.getBlockState(target).isAir() && level.getBlockState(target).getBlock() != Blocks.BEDROCK) {
                        level.destroyBlock(target, true);
                        broken++;
                    }
                }
            }
        }
    }

    static class LiquidFiller extends AdminModule {
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.EnumSetting block;
        int tick = 0;
        LiquidFiller() { super("liquid_filler", "LiquidFiller", "Fills nearby liquid sources", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 4, 1, 8, "Fill radius");
            block = enumVal("Block", "Cobblestone", List.of("Cobblestone", "Stone", "Glass"), "Block to fill with");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            int r = radius.getValue();
            net.minecraft.world.level.block.state.BlockState fillBlock = switch (block.getValue()) {
                case "Stone" -> Blocks.STONE.defaultBlockState();
                case "Glass" -> Blocks.GLASS.defaultBlockState();
                default -> Blocks.COBBLESTONE.defaultBlockState();
            };
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (level.getBlockState(pos).getBlock() == Blocks.WATER || level.getBlockState(pos).getBlock() == Blocks.LAVA) {
                            level.setBlock(pos, fillBlock, 3);
                        }
                    }
                }
            }
        }
    }

    static class NoGhostBlocks extends AdminModule {
        int tick = 0;
        NoGhostBlocks() { super("no_ghost_blocks", "NoGhostBlocks", "Re-sends block updates to prevent desync", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            int r = 4;
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var state = level.getBlockState(pos);
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        }
    }

    static class AutoSmelt extends AdminModule {
        private ModuleSetting.BoolSetting ores;
        private ModuleSetting.BoolSetting food;
        int tick = 0;
        AutoSmelt() { super("auto_smelt", "AutoSmelt", "Converts raw ores and food in inventory to smelted versions", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            ores = bool("Ores", true, "Smelt ores and materials");
            food = bool("Food", true, "Cook raw food items");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                ItemStack replacement = getSmeltResult(stack);
                if (replacement != null) {
                    player.getInventory().setItem(i, replacement);
                }
            }
        }

        private ItemStack getSmeltResult(ItemStack stack) {
            if (ores.getValue()) {
                if (stack.is(Items.RAW_IRON)) return new ItemStack(Items.IRON_INGOT, stack.getCount());
                if (stack.is(Items.RAW_GOLD)) return new ItemStack(Items.GOLD_INGOT, stack.getCount());
                if (stack.is(Items.RAW_COPPER)) return new ItemStack(Items.COPPER_INGOT, stack.getCount());
                if (stack.is(Items.COBBLESTONE)) return new ItemStack(Items.STONE, stack.getCount());
                if (stack.is(Items.SAND)) return new ItemStack(Items.GLASS, stack.getCount());
                if (stack.is(Items.CLAY_BALL)) return new ItemStack(Items.BRICK, stack.getCount());
            }
            if (food.getValue()) {
                if (stack.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF, stack.getCount());
                if (stack.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP, stack.getCount());
            }
            return null;
        }
    }

    static class AutoFarm extends AdminModule {
        private ModuleSetting.IntSetting radius;
        int tick = 0;
        AutoFarm() { super("auto_farm", "AutoFarm", "Auto-harvests and replants crops", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 6, 2, 16, "Farm radius");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            int r = radius.getValue();
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    for (int y = -2; y <= 2; y++) {
                        BlockPos check = center.offset(x, y, z);
                        if (level.getBlockState(check).getBlock() instanceof CropBlock crop) {
                            if (crop.isMaxAge(level.getBlockState(check))) {
                                level.destroyBlock(check, true);
                                level.setBlock(check, crop.defaultBlockState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    static class EChestFarmer extends AdminModule {
        int tick = 0;
        EChestFarmer() { super("echest_farmer", "EChestFarmer", "Breaks ender chests in range to farm obsidian", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            int r = 8;
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (level.getBlockState(pos).getBlock() == Blocks.ENDER_CHEST) {
                            level.destroyBlock(pos, true);
                            return; // One per cycle
                        }
                    }
                }
            }
        }
    }

    static class AutoBrush extends AdminModule {
        int tick = 0;
        AutoBrush() { super("auto_brush", "AutoBrush", "Breaks suspicious sand/gravel blocks nearby for loot", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            int r = 4;
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var block = level.getBlockState(pos).getBlock();
                        if (block == Blocks.SUSPICIOUS_SAND || block == Blocks.SUSPICIOUS_GRAVEL) {
                            level.destroyBlock(pos, true);
                            return; // One per cycle
                        }
                    }
                }
            }
        }
    }

    static class AirPlace extends AdminModule {
        private ModuleSetting.EnumSetting block;
        private ModuleSetting.BoolSetting consumeBlocks;
        int tick = 0;
        AirPlace() { super("air_place", "AirPlace", "Places blocks at looked-at air position while sneaking", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            block = enumVal("Block", "Cobblestone", List.of("Cobblestone", "Dirt", "Netherrack", "Stone"), "Block type to place");
            consumeBlocks = bool("Consume", true, "Consume blocks from inventory");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            if (!player.isCrouching()) return;
            net.minecraft.world.level.block.state.BlockState placeState = getPlaceBlock();
            net.minecraft.world.item.Item requiredItem = getRequiredItem();
            // Check inventory if consuming
            if (consumeBlocks.getValue()) {
                boolean hasBlock = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).is(requiredItem)) { hasBlock = true; break; }
                }
                if (!hasBlock) return;
            }
            // Raycast up to 5 blocks to find an air position to place at
            Vec3 eye = player.getEyePosition();
            Vec3 look = player.getLookAngle();
            for (double d = 1.0; d <= 5.0; d += 0.5) {
                BlockPos target = BlockPos.containing(eye.add(look.scale(d)));
                if (level.getBlockState(target).isAir()) {
                    level.setBlock(target, placeState, 3);
                    // Consume from inventory
                    if (consumeBlocks.getValue()) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.is(requiredItem)) {
                                stack.shrink(1);
                                if (stack.isEmpty()) player.getInventory().setItem(i, ItemStack.EMPTY);
                                break;
                            }
                        }
                    }
                    return;
                }
            }
        }
        private net.minecraft.world.level.block.state.BlockState getPlaceBlock() {
            return switch (block.getValue()) {
                case "Dirt" -> Blocks.DIRT.defaultBlockState();
                case "Netherrack" -> Blocks.NETHERRACK.defaultBlockState();
                case "Stone" -> Blocks.STONE.defaultBlockState();
                default -> Blocks.COBBLESTONE.defaultBlockState();
            };
        }
        private net.minecraft.world.item.Item getRequiredItem() {
            return switch (block.getValue()) {
                case "Dirt" -> Items.DIRT;
                case "Netherrack" -> Items.NETHERRACK;
                case "Stone" -> Items.STONE;
                default -> Items.COBBLESTONE;
            };
        }
    }

    static class StashFinder extends AdminModule {
        int tick = 0;
        StashFinder() { super("stash_finder", "StashFinder", "Scans for chest clusters (3+ within 5 blocks)", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 200 != 0) return;
            int r = 32;
            BlockPos center = player.blockPosition();
            List<BlockPos> chests = new ArrayList<>();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var block = level.getBlockState(pos).getBlock();
                        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.BARREL) {
                            chests.add(pos);
                        }
                    }
                }
            }
            // Find clusters: chests with 2+ other chests within 5 blocks
            Set<BlockPos> reported = new HashSet<>();
            for (BlockPos chest : chests) {
                if (reported.contains(chest)) continue;
                int nearby = 0;
                List<BlockPos> cluster = new ArrayList<>();
                cluster.add(chest);
                for (BlockPos other : chests) {
                    if (other.equals(chest)) continue;
                    if (chest.closerThan(other, 5.0)) {
                        nearby++;
                        cluster.add(other);
                    }
                }
                if (nearby >= 2) {
                    // Calculate centroid
                    int cx = 0, cy = 0, cz = 0;
                    for (BlockPos p : cluster) { cx += p.getX(); cy += p.getY(); cz += p.getZ(); }
                    cx /= cluster.size(); cy /= cluster.size(); cz /= cluster.size();
                    player.sendSystemMessage(Component.literal(
                        "\u00a7e[StashFinder] \u00a7fCluster of " + cluster.size() + " containers at \u00a7a" + cx + ", " + cy + ", " + cz
                    ));
                    reported.addAll(cluster);
                }
            }
            if (reported.isEmpty()) {
                player.sendSystemMessage(Component.literal("\u00a7e[StashFinder] \u00a77No stash clusters found in 32-block radius."));
            }
        }
    }

    static class HighwayBuilder extends AdminModule {
        private ModuleSetting.IntSetting width;
        private ModuleSetting.IntSetting height;
        int tick = 0;
        HighwayBuilder() { super("highway_builder", "HighwayBuilder", "Auto-builds a tunnel forward", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            width = integer("Width", 3, 1, 5, "Tunnel width");
            height = integer("Height", 4, 3, 6, "Tunnel height");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 2 != 0) return; // Every other tick to reduce load
            Direction facing = player.getDirection();
            BlockPos base = player.blockPosition().relative(facing, 1);
            int built = 0;
            int w = width.getValue();
            int h = height.getValue();
            int halfW = w / 2;
            // Clear a W-wide, H-tall tunnel and place floor
            for (int forward = 0; forward < 3 && built < 20; forward++) {
                BlockPos fwdBase = base.relative(facing, forward);
                Direction left = facing.getCounterClockWise();
                // Build width positions
                List<BlockPos> widthPositions = new ArrayList<>();
                for (int i = -halfW; i <= halfW; i++) {
                    if (i < 0) widthPositions.add(fwdBase.relative(left, -i));
                    else if (i > 0) widthPositions.add(fwdBase.relative(left.getOpposite(), i));
                    else widthPositions.add(fwdBase);
                }
                for (BlockPos wp : widthPositions) {
                    // Place floor below
                    BlockPos floor = wp.below();
                    if (level.getBlockState(floor).isAir()) {
                        level.setBlock(floor, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        built++;
                    }
                    // Clear tunnel
                    for (int cy = 0; cy < h; cy++) {
                        BlockPos clear = wp.above(cy);
                        if (!level.getBlockState(clear).isAir() && level.getBlockState(clear).getBlock() != Blocks.BEDROCK) {
                            level.destroyBlock(clear, true);
                            built++;
                        }
                    }
                }
            }
        }
    }

    static class EndermanLook extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        EndermanLook() { super("enderman_look", "EndermanLook", "Teleports angry endermen away to prevent aggro", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 32, "Detection range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            int r = range.getValue();
            List<EnderMan> endermen = level.getEntitiesOfClass(EnderMan.class, player.getBoundingBox().inflate(r));
            for (EnderMan ender : endermen) {
                // If targeting this player, teleport them away
                if (ender.getTarget() != null && ender.getTarget().equals(player)) {
                    double angle = Math.random() * Math.PI * 2;
                    double dx = Math.cos(angle) * 20.0;
                    double dz = Math.sin(angle) * 20.0;
                    ender.teleportTo(ender.getX() + dx, ender.getY(), ender.getZ() + dz);
                    ender.setTarget(null);
                }
            }
        }
    }

    static class AutoMount extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        AutoMount() { super("auto_mount", "AutoMount", "Auto-mounts nearest rideable entity", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 4, 2, 8, "Search range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 20 != 0) return;
            if (player.isPassenger()) return;
            int r = range.getValue();
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(r));
            Entity closest = null;
            double closestDist = Double.MAX_VALUE;
            for (Entity e : entities) {
                if (e instanceof AbstractHorse || e instanceof Pig) {
                    double dist = e.distanceToSqr(player);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = e;
                    }
                }
            }
            if (closest != null) {
                player.startRiding(closest);
            }
        }
    }

    static class Flattener extends AdminModule {
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.IntSetting height;
        int tick = 0;
        Flattener() { super("flattener", "Flattener", "Flattens terrain around you", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 4, 1, 8, "Flatten radius");
            height = integer("Height", 3, 1, 5, "Max height to clear");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            int r = radius.getValue();
            int h = height.getValue();
            BlockPos feet = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    for (int y = 1; y <= h; y++) {
                        BlockPos pos = feet.offset(x, y, z);
                        if (!level.getBlockState(pos).isAir() && level.getBlockState(pos).getBlock() != Blocks.BEDROCK) {
                            level.destroyBlock(pos, true);
                        }
                    }
                }
            }
        }
    }

    static class AutoNametag extends AdminModule {
        int tick = 0;
        AutoNametag() { super("auto_nametag", "AutoNametag", "Auto-applies nametags to nearby unnamed mobs", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            // Find nametags in inventory
            int nametagSlot = -1;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(Items.NAME_TAG)) {
                    nametagSlot = i;
                    break;
                }
            }
            if (nametagSlot < 0) return;
            // Find nearest unnamed mob within 4 blocks
            List<LivingEntity> mobs = level.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(4),
                e -> e != player && e.isAlive() && !e.hasCustomName());
            if (!mobs.isEmpty()) {
                LivingEntity target = mobs.get(0);
                ItemStack nametag = player.getInventory().getItem(nametagSlot);
                // Use custom name from nametag component, or default
                String name = "Named";
                if (nametag.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                    name = nametag.getHoverName().getString();
                }
                target.setCustomName(Component.literal(name));
                target.setCustomNameVisible(true);
                nametag.shrink(1);
                if (nametag.isEmpty()) {
                    player.getInventory().setItem(nametagSlot, ItemStack.EMPTY);
                }
            }
        }
    }

    static class AutoWither extends AdminModule {
        AutoWither() { super("auto_wither", "AutoWither", "Builds wither structure at player position (one-shot)", ModuleCategory.WORLD); }
        @Override public void onEnable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos base = player.blockPosition().relative(player.getDirection(), 3);
            // T-shape of soul sand: bottom center, then 3 across on top
            level.setBlock(base, Blocks.SOUL_SAND.defaultBlockState(), 3);
            level.setBlock(base.above(), Blocks.SOUL_SAND.defaultBlockState(), 3);
            // Cross bar
            Direction left = player.getDirection().getCounterClockWise();
            Direction right = player.getDirection().getClockWise();
            level.setBlock(base.above().relative(left), Blocks.SOUL_SAND.defaultBlockState(), 3);
            level.setBlock(base.above().relative(right), Blocks.SOUL_SAND.defaultBlockState(), 3);
            // 3 wither skeleton skulls on top of the cross bar
            level.setBlock(base.above(2), Blocks.WITHER_SKELETON_SKULL.defaultBlockState(), 3);
            level.setBlock(base.above(2).relative(left), Blocks.WITHER_SKELETON_SKULL.defaultBlockState(), 3);
            level.setBlock(base.above(2).relative(right), Blocks.WITHER_SKELETON_SKULL.defaultBlockState(), 3);
            player.sendSystemMessage(Component.literal("\u00a7e[AutoWither] \u00a7fWither structure built!"));
            setEnabled(false);
        }
    }

    static class AutoSignEditor extends AdminModule {
        int tick = 0;
        private final Set<BlockPos> writtenSigns = new HashSet<>();
        AutoSignEditor() { super("auto_sign_editor", "AutoSignEditor", "Writes text on nearby signs (each sign written once)", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            int r = 3;
            BlockPos center = player.blockPosition();
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (writtenSigns.contains(pos)) continue;
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof SignBlockEntity) {
                            writtenSigns.add(pos.immutable());
                            // Use data command to set sign text (proven approach)
                            String cmd = String.format(
                                "data merge block %d %d %d {front_text:{messages:['{\"text\":\"Admin\"}','{\"text\":\"was here\"}','{\"text\":\"\"}','{\"text\":\"\"}']}}",
                                pos.getX(), pos.getY(), pos.getZ());
                            level.getServer().getCommands().performPrefixedCommand(
                                player.createCommandSourceStack().withSuppressedOutput(), cmd);
                        }
                    }
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            writtenSigns.clear();
        }
    }

    static class EntityCleaner extends AdminModule {
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.BoolSetting keepItems;
        int tick = 0;
        EntityCleaner() { super("entity_cleaner", "EntityCleaner", "Removes non-player entities nearby", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 16, 4, 32, "Clean radius");
            keepItems = bool("KeepItems", true, "Don't remove dropped items");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            int r = radius.getValue();
            List<Entity> entities = level.getEntities(player, player.getBoundingBox().inflate(r));
            for (Entity e : entities) {
                if (e instanceof ServerPlayer) continue;
                if (keepItems.getValue() && e instanceof net.minecraft.world.entity.item.ItemEntity) continue;
                e.discard();
            }
        }
    }

    static class TreeAura extends AdminModule {
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.BoolSetting leaves;
        int tick = 0;
        TreeAura() { super("tree_aura", "TreeAura", "Auto-breaks trees around you", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            radius = integer("Radius", 6, 2, 16, "Scan radius");
            leaves = bool("Leaves", false, "Also break leaves");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 5 != 0) return;
            int r = radius.getValue();
            BlockPos center = player.blockPosition();
            int broken = 0;
            for (int x = -r; x <= r && broken < 4; x++) {
                for (int y = 0; y <= 10 && broken < 4; y++) {
                    for (int z = -r; z <= r && broken < 4; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        boolean isLog = level.getBlockState(pos).is(net.minecraft.tags.BlockTags.LOGS);
                        boolean isLeaf = leaves.getValue() && level.getBlockState(pos).is(net.minecraft.tags.BlockTags.LEAVES);
                        if (isLog || isLeaf) {
                            level.destroyBlock(pos, true);
                            broken++;
                        }
                    }
                }
            }
        }
    }

    static class PacketCanceller extends AdminModule {
        PacketCanceller() { super("packet_canceller", "PacketCanceller", "Cancels damage from projectiles (arrows, fireballs)", ModuleCategory.WORLD); }
        @Override public void onDamage(ServerPlayer player, LivingDamageEvent.Pre event) {
            // Cancel projectile damage: arrows, fireballs, tridents, shulker bullets, etc.
            if (event.getSource().is(DamageTypes.ARROW)
                || event.getSource().is(DamageTypes.FIREBALL)
                || event.getSource().is(DamageTypes.WITHER_SKULL)
                || event.getSource().is(DamageTypes.TRIDENT)
                || event.getSource().is(DamageTypes.MOB_PROJECTILE)
                || event.getSource().is(DamageTypes.UNATTRIBUTED_FIREBALL)) {
                event.setNewDamage(0);
            }
        }
    }

    // ======================== NEW METEOR-INSPIRED MODULES ========================

    static class Ambience extends AdminModule {
        private ModuleSetting.EnumSetting mode;
        int tick = 0;
        Ambience() { super("ambience", "Ambience", "Sets weather and time for atmosphere", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            mode = enumVal("Mode", "Day", List.of("Day", "Night", "Sunset", "Storm"), "Ambience mode");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return; // Re-apply every 5 seconds to hold the time/weather
            String m = mode.getValue();
            switch (m) {
                case "Day" -> {
                    level.setDayTime(1000);
                    level.setWeatherParameters(6000, 0, false, false);
                }
                case "Night" -> {
                    level.setDayTime(13000);
                    level.setWeatherParameters(6000, 0, false, false);
                }
                case "Sunset" -> {
                    level.setDayTime(12000);
                    level.setWeatherParameters(6000, 0, false, false);
                }
                case "Storm" -> {
                    level.setWeatherParameters(0, 6000, true, true);
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            level.setWeatherParameters(6000, 0, false, false);
        }
    }

    static class AutoBrewer extends AdminModule {
        int tick = 0;
        AutoBrewer() { super("auto_brewer", "AutoBrewer", "Auto-brews potions in nearby brewing stands", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 4 != 0) return;
            BlockPos center = player.blockPosition();
            int radius = 5;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof net.minecraft.world.level.block.entity.BrewingStandBlockEntity brewStand) {
                            // Speed up brewing: tick the brewing stand multiple times
                            // This mimics Meteor's approach of accelerating nearby brewing stands
                            for (int i = 0; i < 20; i++) {
                                net.minecraft.world.level.block.entity.BrewingStandBlockEntity.serverTick(level, pos, level.getBlockState(pos), brewStand);
                            }
                        }
                    }
                }
            }
        }
    }

    static class AutoSmelter extends AdminModule {
        private ModuleSetting.BoolSetting ores;
        private ModuleSetting.BoolSetting food;
        private ModuleSetting.BoolSetting misc;
        int tick = 0;
        AutoSmelter() { super("auto_smelter", "AutoSmelter", "Converts raw materials in inventory (extended)", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            ores = bool("Ores", true, "Smelt ores and raw metals");
            food = bool("Food", true, "Cook raw food items");
            misc = bool("Misc", true, "Smelt misc items (sponge, cactus, kelp, etc)");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 40 != 0) return;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                ItemStack replacement = getSmeltResult(stack);
                if (replacement != null) {
                    player.getInventory().setItem(i, replacement);
                }
            }
        }

        private ItemStack getSmeltResult(ItemStack stack) {
            if (ores.getValue()) {
                if (stack.is(Items.RAW_IRON)) return new ItemStack(Items.IRON_INGOT, stack.getCount());
                if (stack.is(Items.RAW_GOLD)) return new ItemStack(Items.GOLD_INGOT, stack.getCount());
                if (stack.is(Items.RAW_COPPER)) return new ItemStack(Items.COPPER_INGOT, stack.getCount());
                if (stack.is(Items.COBBLESTONE)) return new ItemStack(Items.STONE, stack.getCount());
                if (stack.is(Items.SAND)) return new ItemStack(Items.GLASS, stack.getCount());
                if (stack.is(Items.CLAY_BALL)) return new ItemStack(Items.BRICK, stack.getCount());
                if (stack.is(Items.IRON_ORE)) return new ItemStack(Items.IRON_INGOT, stack.getCount());
                if (stack.is(Items.GOLD_ORE)) return new ItemStack(Items.GOLD_INGOT, stack.getCount());
                if (stack.is(Items.COPPER_ORE)) return new ItemStack(Items.COPPER_INGOT, stack.getCount());
                if (stack.is(Items.ANCIENT_DEBRIS)) return new ItemStack(Items.NETHERITE_SCRAP, stack.getCount());
            }
            if (food.getValue()) {
                if (stack.is(Items.BEEF)) return new ItemStack(Items.COOKED_BEEF, stack.getCount());
                if (stack.is(Items.PORKCHOP)) return new ItemStack(Items.COOKED_PORKCHOP, stack.getCount());
                if (stack.is(Items.CHICKEN)) return new ItemStack(Items.COOKED_CHICKEN, stack.getCount());
                if (stack.is(Items.MUTTON)) return new ItemStack(Items.COOKED_MUTTON, stack.getCount());
                if (stack.is(Items.RABBIT)) return new ItemStack(Items.COOKED_RABBIT, stack.getCount());
                if (stack.is(Items.COD)) return new ItemStack(Items.COOKED_COD, stack.getCount());
                if (stack.is(Items.SALMON)) return new ItemStack(Items.COOKED_SALMON, stack.getCount());
            }
            if (misc.getValue()) {
                if (stack.is(Items.WET_SPONGE)) return new ItemStack(Items.SPONGE, stack.getCount());
                if (stack.is(Items.CACTUS)) return new ItemStack(Items.GREEN_DYE, stack.getCount());
                if (stack.is(Items.KELP)) return new ItemStack(Items.DRIED_KELP, stack.getCount());
            }
            return null;
        }
    }

    static class BuildHeight extends AdminModule {
        int tick = 0;
        private int lastReportedY = Integer.MIN_VALUE;
        BuildHeight() { super("build_height", "BuildHeight", "Displays current Y level and build height info when Y changes significantly", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 100 != 0) return;
            int y = player.blockPosition().getY();
            // Only report when Y has changed by 5+ blocks to avoid spam
            if (Math.abs(y - lastReportedY) < 5) return;
            lastReportedY = y;
            int maxY = level.getMaxY();
            int minY = level.getMinY();
            int distToMax = maxY - y;
            int distToMin = y - minY;
            player.sendSystemMessage(Component.literal(
                "\u00a7b[BuildHeight] \u00a7fY: \u00a7e" + y
                + " \u00a7f| Max: \u00a7a" + maxY + " \u00a7f(" + distToMax + " above)"
                + " \u00a7f| Min: \u00a7c" + minY + " \u00a7f(" + distToMin + " below)"
            ));
        }
    }

    static class Collisions extends AdminModule {
        Collisions() { super("collisions", "Collisions", "Disables block collisions for world-building (no physics)", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            player.noPhysics = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            player.noPhysics = false;
        }
    }

    static class Flamethrower extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        Flamethrower() { super("flamethrower", "Flamethrower", "Auto-cooks nearby food animals by igniting them", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 10, 4, 24, "Ignite radius");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            double r = range.getValue();
            var aabb = player.getBoundingBox().inflate(r);
            for (Entity entity : level.getEntitiesOfClass(Animal.class, aabb)) {
                if (!entity.isAlive() || entity.isOnFire()) continue;
                // Only ignite animals that drop food (cows, pigs, sheep, chickens, rabbits, etc.)
                if (isFoodAnimal(entity)) {
                    entity.setRemainingFireTicks(80); // 4 seconds - enough to kill and drop cooked food
                }
            }
        }
        private static boolean isFoodAnimal(Entity entity) {
            // Matches Meteor's behavior: ignite every alive piece of food
            return entity instanceof net.minecraft.world.entity.animal.cow.Cow
                || entity instanceof Pig
                || entity instanceof Sheep
                || entity instanceof net.minecraft.world.entity.animal.chicken.Chicken
                || entity instanceof net.minecraft.world.entity.animal.rabbit.Rabbit;
        }
    }

    static class MountBypass extends AdminModule {
        MountBypass() { super("mount_bypass", "MountBypass", "Removes mount speed limits, fly mount by look direction", ModuleCategory.WORLD); }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (!player.isPassenger() || player.getVehicle() == null) return;
            Entity vehicle = player.getVehicle();
            vehicle.setNoGravity(true);
            Vec3 look = player.getLookAngle();
            vehicle.setDeltaMovement(look.scale(1.0));
            vehicle.hurtMarked = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            if (player.isPassenger() && player.getVehicle() != null) {
                player.getVehicle().setNoGravity(false);
            }
        }
    }

    // ======================== NEW METEOR-INSPIRED MODULES (BATCH 2) ========================

    static class InfinityMiner extends AdminModule {
        private ModuleSetting.IntSetting range;
        int tick = 0;
        InfinityMiner() { super("infinity_miner", "InfinityMiner", "Continuous diamond ore mining — destroys diamond ore in range", ModuleCategory.WORLD); }
        @Override protected void initSettings() {
            range = integer("Range", 8, 4, 16, "Mining scan range");
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % 10 != 0) return;
            int r = range.getValue();
            BlockPos center = player.blockPosition();
            BlockPos closest = null;
            double closestDist = Double.MAX_VALUE;
            // Scan for diamond ore (including deepslate variant)
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var block = level.getBlockState(pos).getBlock();
                        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) {
                            double dist = center.distSqr(pos);
                            if (dist < closestDist) {
                                closestDist = dist;
                                closest = pos;
                            }
                        }
                    }
                }
            }
            if (closest != null) {
                // If within 2 blocks, mine it
                double distToTarget = Math.sqrt(center.distSqr(closest));
                if (distToTarget <= 3.0) {
                    level.destroyBlock(closest, true);
                    player.sendSystemMessage(Component.literal("\u00a7b[InfinityMiner] \u00a7fMined diamond ore at \u00a7e"
                        + closest.getX() + ", " + closest.getY() + ", " + closest.getZ()));
                } else {
                    // Move toward the ore
                    double dx = closest.getX() + 0.5 - player.getX();
                    double dy = closest.getY() + 0.5 - player.getY();
                    double dz = closest.getZ() + 0.5 - player.getZ();
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    double speed = 0.4;
                    player.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);
                    player.hurtMarked = true;
                }
            }
        }
    }
}
