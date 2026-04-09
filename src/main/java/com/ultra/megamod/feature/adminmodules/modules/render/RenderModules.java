package com.ultra.megamod.feature.adminmodules.modules.render;

import com.ultra.megamod.feature.adminmodules.AdminModule;
import com.ultra.megamod.feature.adminmodules.ModuleCategory;
import com.ultra.megamod.feature.adminmodules.ModuleSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import com.ultra.megamod.feature.adminmodules.AdminModuleState;

import java.util.*;
import java.util.function.Consumer;

public class RenderModules {

    public static void register(Consumer<AdminModule> reg) {
        reg.accept(new Fullbright());
        reg.accept(new Nametags());
        reg.accept(new StorageESP());
        reg.accept(new LightOverlay());
        reg.accept(new Breadcrumbs());
        reg.accept(new Chams());
        reg.accept(new FreeLook());
        reg.accept(new NoRender());
        reg.accept(new CameraClip());
        reg.accept(new WaypointRender());
        reg.accept(new NewChunks());
        reg.accept(new BlockHighlight());
        reg.accept(new LogoutSpots());
        reg.accept(new CityESP());
        reg.accept(new SkeletonESP());
        reg.accept(new Search());
        reg.accept(new ItemPhysics());
        reg.accept(new CustomFOV());
        reg.accept(new HandView());
        reg.accept(new EntityOwner());
        reg.accept(new TimeChanger());
        reg.accept(new NoBob());
        reg.accept(new NoWeather());
        reg.accept(new TunnelESP());
        reg.accept(new VoidESP());
        reg.accept(new SpawnerFinder());
        reg.accept(new OreScanner());
        reg.accept(new MobOwner());
        reg.accept(new AntiOverlay());
        reg.accept(new Zoom());
        // New Meteor-style render modules
        reg.accept(new BetterTooltips());
        reg.accept(new BreakIndicators());
        reg.accept(new PopChams());
        reg.accept(new WallHack());
        reg.accept(new Trail());
        reg.accept(new Marker());
        reg.accept(new BetterTab());
        reg.accept(new Xray());
        // New Meteor-inspired render modules (batch 2)
        reg.accept(new BlockESP());
        reg.accept(new BlockSelection());
        reg.accept(new Blur());
        reg.accept(new BossStack());
        reg.accept(new CameraTweaks());
        reg.accept(new ItemHighlight());
        // Mixin-backed render modules
        reg.accept(new NoFog());
        reg.accept(new ContainerButtons());
        reg.accept(new AttackUseTick());
    }

    // ========================================================================
    // Fullbright -- Server-side night vision effect
    // ========================================================================
    static class Fullbright extends AdminModule {
        private ModuleSetting.EnumSetting mode;
        private int tickCounter = 0;
        Fullbright() { super("fullbright", "Fullbright", "Full brightness via night vision or gamma override", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            mode = enumVal("Mode", "NightVision", List.of("NightVision", "Gamma"), "NightVision=server effect, Gamma=client mixin override");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            if ("Gamma".equals(mode.getValue())) {
                AdminModuleState.fullbrightGammaEnabled = true;
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Only apply NightVision in NightVision mode; Gamma mode uses client mixin
            if ("Gamma".equals(mode.getValue())) return;
            // Rate limit: refresh every 300 ticks (NV lasts 400 ticks, so 100-tick overlap)
            if (++tickCounter % 300 != 0) return;
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.fullbrightGammaEnabled = false;
            player.removeEffect(MobEffects.NIGHT_VISION);
            tickCounter = 0;
        }
    }

    // ========================================================================
    // Nametags -- Apply Glowing to PLAYERS ONLY so you can see their nametags through walls
    // Differs from Chams: Nametags targets players only at long range, Chams targets ALL entities
    // ========================================================================
    static class Nametags extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        private ModuleSetting.BoolSetting playersOnly;
        private ModuleSetting.BoolSetting throughWalls;
        Nametags() { super("nametags", "Nametags", "Glowing on PLAYERS to see nametags through walls (128-block range, players-only by default)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 128, 16, 256, "Glowing effect range in blocks");
            playersOnly = bool("Players Only", true, "Only apply to players (disable for all entities)");
            throughWalls = bool("Through Walls", true, "Force nametag rendering through blocks (client mixin)");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            if (throughWalls.getValue()) {
                AdminModuleState.nametagThroughWallsEnabled = true;
            }
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 40 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            if (playersOnly.getValue()) {
                List<Player> players = level.getEntitiesOfClass(Player.class, box, e -> e.isAlive() && e != player);
                for (Player p : players) {
                    p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                }
            } else {
                List<Entity> entities = level.getEntities(player, box, e -> e instanceof LivingEntity && e.isAlive() && e != player);
                for (Entity e : entities) {
                    if (e instanceof LivingEntity living) {
                        living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
                    }
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
            AdminModuleState.nametagThroughWallsEnabled = false;
        }
    }

    // ========================================================================
    // StorageESP -- Client-side: highlights containers through walls
    // ========================================================================
    static class StorageESP extends AdminModule {
        private ModuleSetting.IntSetting range;
        private ModuleSetting.IntSetting scanRate;
        private ModuleSetting.BoolSetting chests;
        private ModuleSetting.BoolSetting barrels;
        private ModuleSetting.BoolSetting shulkers;
        private ModuleSetting.BoolSetting enderChests;

        private final List<StoragePos> foundContainers = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<StoragePos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        private record StoragePos(BlockPos pos, float r, float g, float b) {}

        StorageESP() { super("storage_esp", "StorageESP", "Highlights storage containers through walls with colored wireframes", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 32, 8, 48, "Scan range in blocks");
            scanRate = integer("Scan Rate", 4, 1, 20, "Frames between scan slices");
            chests = bool("Chests", true, "Highlight chests and trapped chests");
            barrels = bool("Barrels", true, "Highlight barrels");
            shulkers = bool("Shulkers", true, "Highlight shulker boxes");
            enderChests = bool("Ender Chests", true, "Highlight ender chests");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= scanRate.getValue()) {
                scanTick = 0;
                storageScanIncremental(mc);
            }
            if (foundContainers.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();

                for (StoragePos sp : foundContainers) {
                    float x = sp.pos.getX(), y = sp.pos.getY(), z = sp.pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, sp.r, sp.g, sp.b, 0.7f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void storageScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = range.getValue();
            BlockPos center = mc.player.blockPosition();
            int slicesPerTick = 4;
            if (!scanInProgress) {
                scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true;
            }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var state = mc.level.getBlockState(pos);
                        float[] color = storageGetColor(state);
                        if (color != null) scanBuffer.add(new StoragePos(pos, color[0], color[1], color[2]));
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                foundContainers.clear(); foundContainers.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        private float[] storageGetColor(net.minecraft.world.level.block.state.BlockState state) {
            Block block = state.getBlock();
            if (chests.getValue() && (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST))
                return new float[]{1.0f, 0.8f, 0.0f};
            if (barrels.getValue() && block == Blocks.BARREL)
                return new float[]{0.7f, 0.5f, 0.2f};
            if (enderChests.getValue() && block == Blocks.ENDER_CHEST)
                return new float[]{0.0f, 0.7f, 0.7f};
            if (shulkers.getValue() && state.is(net.minecraft.tags.BlockTags.SHULKER_BOXES))
                return new float[]{0.8f, 0.3f, 0.8f};
            return null;
        }

        @Override public void onDisable(ServerPlayer player) {
            foundContainers.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }
    }

    // ========================================================================
    // LightOverlay -- Client-side: renders crosses at dark mob-spawnable spots
    // ========================================================================
    static class LightOverlay extends AdminModule {
        private ModuleSetting.IntSetting range;
        private ModuleSetting.IntSetting scanRate;

        private final List<BlockPos> darkSpots = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<BlockPos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        LightOverlay() { super("light_overlay", "LightOverlay", "Renders red/yellow crosses at dark mob-spawnable spots", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 24, "Scan range in blocks");
            scanRate = integer("Scan Rate", 10, 2, 40, "Frames between scan slices");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= scanRate.getValue()) {
                scanTick = 0;
                lightScanIncremental(mc);
            }
            if (darkSpots.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH_THIN);
                var matrix = poseStack.last().pose();

                for (BlockPos pos : darkSpots) {
                    float x = pos.getX() + 0.5f, y = pos.getY() + 0.01f, z = pos.getZ() + 0.5f;
                    // Red cross on very dark spots, yellow on borderline
                    int light = mc.level.getMaxLocalRawBrightness(pos);
                    if (light <= 0) {
                        ESPRenderHelper.drawCross(consumer, matrix, x, y, z, 0.4f, 1.0f, 0.0f, 0.0f, 0.8f);
                    } else {
                        ESPRenderHelper.drawCross(consumer, matrix, x, y, z, 0.3f, 1.0f, 1.0f, 0.0f, 0.6f);
                    }
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void lightScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = range.getValue();
            BlockPos center = mc.player.blockPosition();
            int slicesPerTick = 4;
            if (!scanInProgress) {
                scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true;
            }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (mc.level.getBlockState(pos).isAir()
                                && mc.level.getBlockState(pos.below()).isSolidRender()
                                && mc.level.getMaxLocalRawBrightness(pos) < 8) {
                            scanBuffer.add(pos);
                        }
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                darkSpots.clear(); darkSpots.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            darkSpots.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }
    }

    // ========================================================================
    // Breadcrumbs -- Leaves END_ROD particle trail behind you (server-side)
    // Differs from Trail: Breadcrumbs uses soft glowing END_ROD particles, Trail uses FLAME
    // ========================================================================
    static class Breadcrumbs extends AdminModule {
        private ModuleSetting.IntSetting interval;
        private int tick = 0;
        Breadcrumbs() { super("breadcrumbs", "Breadcrumbs", "Leaves a glowing particle trail (END_ROD) behind you to retrace your path", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            interval = integer("Interval", 10, 2, 40, "Ticks between trail markers");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % interval.getValue() != 0) return;
            level.sendParticles((net.minecraft.core.particles.ParticleOptions) net.minecraft.core.particles.ParticleTypes.END_ROD,
                player.getX(), player.getY() + 0.1, player.getZ(), 1, 0, 0, 0, 0);
        }
        @Override public void onDisable(ServerPlayer player) {
            tick = 0;
        }
    }

    // ========================================================================
    // Chams -- Apply Glowing effect to all living entities (entity wallhack)
    // Differs from WallHack: Chams targets ALL entities, WallHack targets players only
    // ========================================================================
    static class Chams extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        Chams() { super("chams", "Chams", "Glowing on ALL entities (mobs+players) to see through walls", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 32, 8, 64, "Effect range in blocks");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 20 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive() && e != player);
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // FreeLook -- Decouple camera from player head using NeoForge camera events
    // Hold middle mouse to free-look; release to snap back
    // ========================================================================
    static class FreeLook extends AdminModule {
        FreeLook() { super("free_look", "FreeLook", "Hold middle mouse to look around without turning your character", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.freeLookEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.freeLookEnabled = false;
            AdminModuleState.freeLookActive = false;
        }
    }

    // ========================================================================
    // NoRender -- Remove clutter entities with individual type toggles
    // WARNING: This is DESTRUCTIVE -- it permanently removes entities from the world!
    // ========================================================================
    static class NoRender extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        private ModuleSetting.BoolSetting items;
        private ModuleSetting.BoolSetting xpOrbs;
        private ModuleSetting.BoolSetting arrows;
        private ModuleSetting.BoolSetting fallingBlocks;
        NoRender() { super("no_render", "NoRender", "DESTRUCTIVE: Permanently removes clutter entities. Configure which types to clear", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 32, 8, 64, "Clear range in blocks");
            items = bool("Items", false, "Clear dropped items (WARNING: destroys loot!)");
            xpOrbs = bool("XP Orbs", true, "Clear experience orbs");
            arrows = bool("Arrows", true, "Clear stuck arrows and projectiles");
            fallingBlocks = bool("Falling Blocks", true, "Clear falling block entities");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 60 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<Entity> entities = level.getEntities(player, box, e -> {
                if (e instanceof ItemEntity && items.getValue()) return true;
                if (e instanceof ExperienceOrb && xpOrbs.getValue()) return true;
                if (e instanceof Projectile && arrows.getValue()) return true;
                if (e instanceof FallingBlockEntity && fallingBlocks.getValue()) return true;
                return false;
            });
            for (Entity e : entities) {
                e.discard();
            }
            if (!entities.isEmpty()) {
                player.sendSystemMessage(Component.literal("\u00a76[NoRender] \u00a7fCleared \u00a7e" + entities.size() + " \u00a7fclutter entities"));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // CameraClip -- Camera passes through blocks in third person (via mixin)
    // ========================================================================
    static class CameraClip extends AdminModule {
        CameraClip() { super("camera_clip", "CameraClip", "Camera passes through blocks in third-person view", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.cameraClipEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.cameraClipEnabled = false;
        }
    }

    // ========================================================================
    // WaypointRender -- Renders beacon-style vertical lines at death/spawn
    // ========================================================================
    static class WaypointRender extends AdminModule {
        WaypointRender() { super("waypoint_render", "WaypointRender", "Renders glowing vertical beams at spawn and last death location", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                // Draw beam at world origin (0, 0) as a reference point
                drawVerticalBeam(consumer, matrix, 0.5f, 0.5f, 0.2f, 1.0f, 0.2f);
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
        private void drawVerticalBeam(com.mojang.blaze3d.vertex.VertexConsumer consumer, org.joml.Matrix4f matrix,
                                       float x, float z, float r, float g, float b) {
            for (int y = -64; y < 320; y += 4) {
                consumer.addVertex(matrix, x, y, z).setColor(r, g, b, 0.5f).setNormal(0, 1, 0).setLineWidth(2.0f);
                consumer.addVertex(matrix, x, y + 4, z).setColor(r, g, b, 0.5f).setNormal(0, 1, 0).setLineWidth(2.0f);
            }
        }
    }

    // ========================================================================
    // NewChunks -- Renders chunk grid lines around player to visualize boundaries
    // ========================================================================
    static class NewChunks extends AdminModule {
        NewChunks() { super("new_chunks", "NewChunks", "Renders chunk boundaries as grid lines around the player", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                // Draw chunk boundaries in a 3-chunk radius around the player
                int playerChunkX = mc.player.blockPosition().getX() >> 4;
                int playerChunkZ = mc.player.blockPosition().getZ() >> 4;
                float y = (float) mc.player.getY();
                for (int cx = playerChunkX - 3; cx <= playerChunkX + 4; cx++) {
                    float x = cx * 16;
                    consumer.addVertex(matrix, x, y - 2, (playerChunkZ - 3) * 16).setColor(0.0f, 0.8f, 1.0f, 0.5f).setNormal(0, 0, 1).setLineWidth(1.0f);
                    consumer.addVertex(matrix, x, y - 2, (playerChunkZ + 4) * 16).setColor(0.0f, 0.8f, 1.0f, 0.5f).setNormal(0, 0, 1).setLineWidth(1.0f);
                }
                for (int cz = playerChunkZ - 3; cz <= playerChunkZ + 4; cz++) {
                    float z = cz * 16;
                    consumer.addVertex(matrix, (playerChunkX - 3) * 16, y - 2, z).setColor(0.0f, 0.8f, 1.0f, 0.5f).setNormal(1, 0, 0).setLineWidth(1.0f);
                    consumer.addVertex(matrix, (playerChunkX + 4) * 16, y - 2, z).setColor(0.0f, 0.8f, 1.0f, 0.5f).setNormal(1, 0, 0).setLineWidth(1.0f);
                }
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
    }

    // ========================================================================
    // BlockHighlight -- Custom block selection highlight color (via NeoForge event)
    // ========================================================================
    static class BlockHighlight extends AdminModule {
        private ModuleSetting.EnumSetting color;
        BlockHighlight() { super("block_highlight", "BlockHighlight", "Custom block selection highlight color", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            color = enumVal("Color", "White", List.of("White", "Red", "Green", "Blue", "Yellow", "Cyan", "Magenta"), "Outline color");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.blockHighlightEnabled = true;
            applyColor();
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.blockHighlightEnabled = false;
        }
        private void applyColor() {
            switch (color.getValue()) {
                case "Red" -> { AdminModuleState.blockHighlightR = 1; AdminModuleState.blockHighlightG = 0.2f; AdminModuleState.blockHighlightB = 0.2f; }
                case "Green" -> { AdminModuleState.blockHighlightR = 0.2f; AdminModuleState.blockHighlightG = 1; AdminModuleState.blockHighlightB = 0.2f; }
                case "Blue" -> { AdminModuleState.blockHighlightR = 0.2f; AdminModuleState.blockHighlightG = 0.2f; AdminModuleState.blockHighlightB = 1; }
                case "Yellow" -> { AdminModuleState.blockHighlightR = 1; AdminModuleState.blockHighlightG = 1; AdminModuleState.blockHighlightB = 0.2f; }
                case "Cyan" -> { AdminModuleState.blockHighlightR = 0.2f; AdminModuleState.blockHighlightG = 1; AdminModuleState.blockHighlightB = 1; }
                case "Magenta" -> { AdminModuleState.blockHighlightR = 1; AdminModuleState.blockHighlightG = 0.2f; AdminModuleState.blockHighlightB = 1; }
                default -> { AdminModuleState.blockHighlightR = 1; AdminModuleState.blockHighlightG = 1; AdminModuleState.blockHighlightB = 1; }
            }
        }
    }

    // ========================================================================
    // LogoutSpots -- Record and report player disconnect positions
    // NOTE: recordLogout() must be called from a PlayerLoggedOutEvent handler
    // ========================================================================
    static class LogoutSpots extends AdminModule {
        private final LinkedList<LogoutEntry> logoutPositions = new LinkedList<>();
        private int tickCounter = 0;
        private int lastReportedSize = 0;
        private static final int MAX_ENTRIES = 10;

        private record LogoutEntry(String name, double x, double y, double z, long timestamp) {}

        LogoutSpots() { super("logout_spots", "LogoutSpots", "Records where players log out. Requires external disconnect event hook to call recordLogout()", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }

        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 200 != 0) return;
            // Only report when new logouts have been recorded since last report
            if (!logoutPositions.isEmpty() && logoutPositions.size() != lastReportedSize) {
                lastReportedSize = logoutPositions.size();
                player.sendSystemMessage(Component.literal("\u00a76[LogoutSpots] \u00a7fRecent logout positions:"));
                for (LogoutEntry entry : logoutPositions) {
                    long ago = (System.currentTimeMillis() - entry.timestamp) / 1000;
                    player.sendSystemMessage(Component.literal(String.format(
                            "\u00a77  - \u00a7f%s \u00a77at \u00a7e%.0f, %.0f, %.0f \u00a77(%ds ago)",
                            entry.name, entry.x, entry.y, entry.z, ago)));
                }
            }
        }

        /** Called externally when a player disconnects to record their position */
        public void recordLogout(String playerName, Vec3 pos) {
            logoutPositions.addFirst(new LogoutEntry(playerName, pos.x, pos.y, pos.z, System.currentTimeMillis()));
            while (logoutPositions.size() > MAX_ENTRIES) {
                logoutPositions.removeLast();
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // CityESP -- Highlights breakable blocks in player surrounds (PvP)
    // ========================================================================
    static class CityESP extends AdminModule {
        CityESP() { super("city_esp", "CityESP", "Highlights breakable obsidian surround blocks near other players", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                // For each player, check 4 surround positions (N/S/E/W) for non-obsidian
                for (var player : mc.level.players()) {
                    if (player == mc.player || player.distanceTo(mc.player) > 16) continue;
                    BlockPos base = player.blockPosition();
                    BlockPos[] surround = {base.north(), base.south(), base.east(), base.west()};
                    for (BlockPos pos : surround) {
                        Block block = mc.level.getBlockState(pos).getBlock();
                        if (block != Blocks.OBSIDIAN && block != Blocks.BEDROCK) {
                            // Highlight breakable position in red
                            float x1 = pos.getX(), y1 = pos.getY(), z1 = pos.getZ();
                            drawSmallBox(consumer, matrix, x1, y1, z1, x1+1, y1+1, z1+1, 1.0f, 0.0f, 0.0f);
                        }
                    }
                }
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
        private void drawSmallBox(com.mojang.blaze3d.vertex.VertexConsumer c, org.joml.Matrix4f m,
                float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b) {
            c.addVertex(m,x1,y1,z1).setColor(r,g,b,0.8f).setNormal(1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z1).setColor(r,g,b,0.8f).setNormal(1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z1).setColor(r,g,b,0.8f).setNormal(0,0,1).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z2).setColor(r,g,b,0.8f).setNormal(0,0,1).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z2).setColor(r,g,b,0.8f).setNormal(-1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y1,z2).setColor(r,g,b,0.8f).setNormal(-1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y1,z2).setColor(r,g,b,0.8f).setNormal(0,0,-1).setLineWidth(1.0f);
            c.addVertex(m,x1,y1,z1).setColor(r,g,b,0.8f).setNormal(0,0,-1).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z1).setColor(r,g,b,0.8f).setNormal(1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z1).setColor(r,g,b,0.8f).setNormal(1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z1).setColor(r,g,b,0.8f).setNormal(0,0,1).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z2).setColor(r,g,b,0.8f).setNormal(0,0,1).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z2).setColor(r,g,b,0.8f).setNormal(-1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z2).setColor(r,g,b,0.8f).setNormal(-1,0,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z2).setColor(r,g,b,0.8f).setNormal(0,0,-1).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z1).setColor(r,g,b,0.8f).setNormal(0,0,-1).setLineWidth(1.0f);
            c.addVertex(m,x1,y1,z1).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z1).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z1).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z1).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y1,z2).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x2,y2,z2).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y1,z2).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
            c.addVertex(m,x1,y2,z2).setColor(r,g,b,0.8f).setNormal(0,1,0).setLineWidth(1.0f);
        }
    }

    // ========================================================================
    // SkeletonESP -- Apply Glowing to skeleton-type mobs
    // ========================================================================
    static class SkeletonESP extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        SkeletonESP() { super("skeleton_esp", "SkeletonESP", "Glowing effect on all skeleton-type mobs", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 64, 16, 128, "Effect range in blocks");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        private static final Set<EntityType<?>> SKELETON_TYPES = Set.of(
                EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON, EntityType.BOGGED
        );

        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 40 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<Monster> skeletons = level.getEntitiesOfClass(Monster.class, box,
                    e -> e.isAlive() && SKELETON_TYPES.contains(e.getType()));
            for (Monster skel : skeletons) {
                skel.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0, false, false));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // Search -- Client-side: highlights specific blocks through walls
    // ========================================================================
    static class Search extends AdminModule {
        private ModuleSetting.EnumSetting blockType;
        private ModuleSetting.IntSetting range;
        private ModuleSetting.IntSetting scanRate;

        private final List<BlockPos> foundBlocks = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<BlockPos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        Search() { super("search", "Search", "Highlights specific blocks through walls with wireframes", ModuleCategory.RENDER); }

        @Override protected void initSettings() {
            blockType = enumVal("Block", "Diamond Ore", List.of(
                    "Diamond Ore", "Spawner", "Ancient Debris", "Emerald Ore", "Chest", "End Portal Frame"
            ), "Block type to search for");
            range = integer("Range", 32, 8, 48, "Scan range in blocks");
            scanRate = integer("Scan Rate", 4, 1, 20, "Frames between scan slices");
        }

        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        private Set<Block> getTargetBlocks() {
            return switch (blockType.getValue()) {
                case "Diamond Ore" -> Set.of(Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE);
                case "Spawner" -> Set.of(Blocks.SPAWNER);
                case "Ancient Debris" -> Set.of(Blocks.ANCIENT_DEBRIS);
                case "Emerald Ore" -> Set.of(Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE);
                case "Chest" -> Set.of(Blocks.CHEST, Blocks.TRAPPED_CHEST);
                case "End Portal Frame" -> Set.of(Blocks.END_PORTAL_FRAME);
                default -> Set.of();
            };
        }

        private float[] getSearchColor() {
            return switch (blockType.getValue()) {
                case "Diamond Ore" -> new float[]{0.2f, 0.9f, 0.9f};
                case "Spawner" -> new float[]{1.0f, 0.0f, 0.5f};
                case "Ancient Debris" -> new float[]{0.6f, 0.3f, 0.2f};
                case "Emerald Ore" -> new float[]{0.2f, 1.0f, 0.2f};
                case "Chest" -> new float[]{1.0f, 0.8f, 0.0f};
                case "End Portal Frame" -> new float[]{0.5f, 0.0f, 1.0f};
                default -> new float[]{1.0f, 1.0f, 1.0f};
            };
        }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= scanRate.getValue()) { scanTick = 0; searchScanIncremental(mc); }
            if (foundBlocks.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                float[] rgb = getSearchColor();

                for (BlockPos pos : foundBlocks) {
                    float x = pos.getX(), y = pos.getY(), z = pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, rgb[0], rgb[1], rgb[2], 0.8f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void searchScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = range.getValue();
            BlockPos center = mc.player.blockPosition();
            Set<Block> targets = getTargetBlocks();
            int slicesPerTick = 4;
            if (!scanInProgress) { scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true; }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (targets.contains(mc.level.getBlockState(pos).getBlock())) scanBuffer.add(pos);
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                foundBlocks.clear(); foundBlocks.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            foundBlocks.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }
    }

    // ========================================================================
    // ItemPhysics -- Dropped items lay flat on ground (via mixin)
    // ========================================================================
    static class ItemPhysics extends AdminModule {
        ItemPhysics() { super("item_physics", "ItemPhysics", "Dropped items lay flat on the ground instead of floating and spinning", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.itemPhysicsEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.itemPhysicsEnabled = false;
        }
    }

    // ========================================================================
    // CustomFOV -- Override FOV to a specific value (via NeoForge event)
    // ========================================================================
    static class CustomFOV extends AdminModule {
        private ModuleSetting.IntSetting fov;
        CustomFOV() { super("custom_fov", "CustomFOV", "Override field of view to a custom value", ModuleCategory.RENDER); }
        @Override protected void initSettings() { fov = integer("FOV", 90, 30, 170, "Field of view value"); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.customFovEnabled = true;
            AdminModuleState.customFovValue = fov.getValue();
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.customFovEnabled = false;
        }
    }

    // ========================================================================
    // HandView -- Customize hand rendering position/scale (via NeoForge event)
    // ========================================================================
    static class HandView extends AdminModule {
        private ModuleSetting.DoubleSetting scale;
        private ModuleSetting.DoubleSetting xOff;
        private ModuleSetting.DoubleSetting yOff;
        HandView() { super("hand_view", "HandView", "Customize hand rendering position and scale", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            scale = decimal("Scale", 1.0, 0.1, 2.0, "Hand model scale");
            xOff = decimal("X Offset", 0.0, -1.0, 1.0, "Horizontal offset");
            yOff = decimal("Y Offset", 0.0, -1.0, 1.0, "Vertical offset");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.handViewEnabled = true;
            AdminModuleState.handViewScale = scale.getValue().floatValue();
            AdminModuleState.handViewX = xOff.getValue().floatValue();
            AdminModuleState.handViewY = yOff.getValue().floatValue();
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.handViewEnabled = false;
        }
    }

    // ========================================================================
    // EntityOwner -- Scan for tamed animals, report owners and entity details
    // Shows entity type, custom name, owner, position, and health
    // ========================================================================
    static class EntityOwner extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        EntityOwner() { super("entity_owner", "EntityOwner", "Reports tamed entities with detailed info (name, owner, HP, position)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 64, "Scan range in blocks");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 100 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> owned = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e.isAlive() && e instanceof OwnableEntity oe && oe.getOwner() != null);
            if (!owned.isEmpty()) {
                player.sendSystemMessage(Component.literal("\u00a76[EntityOwner] \u00a7fTamed entities nearby (" + owned.size() + "):"));
                for (LivingEntity animal : owned) {
                    OwnableEntity oe = (OwnableEntity) animal;
                    LivingEntity owner = oe.getOwner();
                    String ownerStr = owner != null ? owner.getName().getString() : "unknown";
                    String name = animal.hasCustomName() ? animal.getCustomName().getString() : animal.getType().getDescription().getString();
                    player.sendSystemMessage(Component.literal(String.format(
                            "\u00a77  - \u00a7f%s \u00a77[\u00a7a%.0f\u00a77/\u00a7a%.0f HP\u00a77] owned by \u00a7e%s \u00a77at \u00a7e%.0f, %.0f, %.0f",
                            name, animal.getHealth(), animal.getMaxHealth(), ownerStr, animal.getX(), animal.getY(), animal.getZ())));
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // TimeChanger -- Set the world time to the configured value
    // WARNING: This affects ALL players on the server, not just the admin!
    // ========================================================================
    static class TimeChanger extends AdminModule {
        private ModuleSetting.IntSetting time;
        private int tickCounter = 0;
        TimeChanger() { super("time_changer", "TimeChanger", "Sets world time (WARNING: affects ALL players, not just you)", ModuleCategory.RENDER); }
        @Override protected void initSettings() { time = integer("Time", 6000, 0, 24000, "World time (0=dawn, 6000=noon, 12000=dusk, 18000=midnight)"); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            // Only set time every 100 ticks (5 seconds) to avoid fighting the server every tick
            if (++tickCounter % 100 != 0) return;
            level.setDayTime(time.getValue());
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // NoBob -- Removes view bobbing entirely (via mixin that cancels bobView)
    // ========================================================================
    static class NoBob extends AdminModule {
        NoBob() { super("no_bob", "NoBob", "Completely removes view bobbing when walking/sprinting", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.noBobEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.noBobEnabled = false;
        }
    }

    // ========================================================================
    // NoWeather -- Clears rain/thunder with server authority
    // WARNING: This affects ALL players on the server!
    // ========================================================================
    static class NoWeather extends AdminModule {
        NoWeather() { super("no_weather", "NoWeather", "Clears rain/snow/thunder (WARNING: affects ALL players)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (level.isRaining() || level.isThundering()) {
                level.setWeatherParameters(6000, 0, false, false);
            }
        }
    }

    // ========================================================================
    // TunnelESP -- Highlights underground air pockets (caves/tunnels)
    // ========================================================================
    static class TunnelESP extends AdminModule {
        private ModuleSetting.IntSetting range;
        TunnelESP() { super("tunnel_esp", "TunnelESP", "Highlights underground air pockets visible through walls", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 32, "Scan range");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            // Only scan underground (below Y 50)
            if (mc.player.getY() > 50) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                int r = range.getValue();
                BlockPos center = mc.player.blockPosition();
                int drawn = 0;
                // Sample every 2 blocks to reduce lag
                for (int x = -r; x <= r; x += 2) {
                    for (int y = -r; y <= r; y += 2) {
                        for (int z = -r; z <= r; z += 2) {
                            if (drawn > 200) break; // Cap rendering to prevent lag
                            BlockPos pos = center.offset(x, y, z);
                            // Air block surrounded by at least 3 solid faces = cave pocket
                            if (mc.level.getBlockState(pos).isAir()) {
                                int solidNeighbors = 0;
                                if (!mc.level.getBlockState(pos.above()).isAir()) solidNeighbors++;
                                if (!mc.level.getBlockState(pos.below()).isAir()) solidNeighbors++;
                                if (!mc.level.getBlockState(pos.north()).isAir()) solidNeighbors++;
                                if (!mc.level.getBlockState(pos.south()).isAir()) solidNeighbors++;
                                if (!mc.level.getBlockState(pos.east()).isAir()) solidNeighbors++;
                                if (!mc.level.getBlockState(pos.west()).isAir()) solidNeighbors++;
                                if (solidNeighbors >= 4) {
                                    float px = pos.getX() + 0.5f, py = pos.getY() + 0.5f, pz = pos.getZ() + 0.5f;
                                    consumer.addVertex(matrix, px-0.1f, py, pz).setColor(0.0f, 1.0f, 0.5f, 0.6f).setNormal(1,0,0).setLineWidth(1.0f);
                                    consumer.addVertex(matrix, px+0.1f, py, pz).setColor(0.0f, 1.0f, 0.5f, 0.6f).setNormal(1,0,0).setLineWidth(1.0f);
                                    drawn++;
                                }
                            }
                        }
                    }
                }
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
    }

    // ========================================================================
    // VoidESP -- Highlights void gaps (air columns below min build height)
    // ========================================================================
    static class VoidESP extends AdminModule {
        VoidESP() { super("void_esp", "VoidESP", "Highlights blocks at edges of void gaps with red markers", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                BlockPos center = mc.player.blockPosition();
                int minY = mc.level.getMinY();
                int drawn = 0;
                // Scan a 16-block radius at foot level for void holes
                for (int x = -16; x <= 16; x++) {
                    for (int z = -16; z <= 16; z++) {
                        if (drawn > 100) break;
                        BlockPos surfacePos = center.offset(x, 0, z);
                        // Check downward for void: if all blocks below are air down to minY
                        boolean isVoid = true;
                        for (int dy = -1; dy >= -5; dy--) {
                            BlockPos checkPos = surfacePos.offset(0, dy, 0);
                            if (checkPos.getY() < minY) break;
                            if (!mc.level.getBlockState(checkPos).isAir()) {
                                isVoid = false;
                                break;
                            }
                        }
                        if (isVoid && surfacePos.getY() - 5 <= minY) {
                            float px = surfacePos.getX() + 0.5f, py = surfacePos.getY(), pz = surfacePos.getZ() + 0.5f;
                            // Draw red X marker
                            consumer.addVertex(matrix, px-0.3f, py, pz-0.3f).setColor(1.0f, 0.0f, 0.0f, 0.8f).setNormal(1,0,1).setLineWidth(2.0f);
                            consumer.addVertex(matrix, px+0.3f, py, pz+0.3f).setColor(1.0f, 0.0f, 0.0f, 0.8f).setNormal(1,0,1).setLineWidth(2.0f);
                            consumer.addVertex(matrix, px+0.3f, py, pz-0.3f).setColor(1.0f, 0.0f, 0.0f, 0.8f).setNormal(-1,0,1).setLineWidth(2.0f);
                            consumer.addVertex(matrix, px-0.3f, py, pz+0.3f).setColor(1.0f, 0.0f, 0.0f, 0.8f).setNormal(-1,0,1).setLineWidth(2.0f);
                            drawn++;
                        }
                    }
                }
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
    }

    // ========================================================================
    // SpawnerFinder -- Client-side: highlights spawner blocks through walls
    // ========================================================================
    static class SpawnerFinder extends AdminModule {
        private ModuleSetting.IntSetting range;
        private final List<BlockPos> foundSpawners = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<BlockPos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        SpawnerFinder() { super("spawner_finder", "SpawnerFinder", "Highlights mob spawners through walls with magenta wireframes", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 32, 8, 48, "Scan range in blocks");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= 4) { scanTick = 0; spawnerScanIncremental(mc); }
            if (foundSpawners.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();

                for (BlockPos pos : foundSpawners) {
                    float x = pos.getX(), y = pos.getY(), z = pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, 1.0f, 0.0f, 0.5f, 0.9f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void spawnerScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = range.getValue();
            BlockPos center = mc.player.blockPosition();
            int slicesPerTick = 4;
            if (!scanInProgress) { scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true; }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (mc.level.getBlockState(pos).getBlock() == Blocks.SPAWNER) scanBuffer.add(pos);
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                foundSpawners.clear(); foundSpawners.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            foundSpawners.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }
    }

    // ========================================================================
    // OreScanner -- Client-side: highlights ores in look direction (64-block ray)
    // ========================================================================
    static class OreScanner extends AdminModule {
        private final List<OreHit> foundOres = new ArrayList<>();
        private int scanTick = 0;

        private record OreHit(BlockPos pos, float r, float g, float b) {}

        private static final Map<Block, float[]> ORE_COLORS = new HashMap<>();
        static {
            float[] diamond = {0.2f, 0.9f, 0.9f}, gold = {1.0f, 0.85f, 0.0f};
            float[] iron = {0.85f, 0.7f, 0.6f}, copper = {0.9f, 0.55f, 0.3f};
            float[] emerald = {0.2f, 1.0f, 0.2f}, lapis = {0.2f, 0.3f, 1.0f};
            float[] redstone = {1.0f, 0.0f, 0.0f}, coal = {0.4f, 0.4f, 0.4f};
            float[] debris = {0.6f, 0.3f, 0.2f}, quartz = {1.0f, 1.0f, 0.9f};
            ORE_COLORS.put(Blocks.DIAMOND_ORE, diamond); ORE_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, diamond);
            ORE_COLORS.put(Blocks.GOLD_ORE, gold); ORE_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE, gold); ORE_COLORS.put(Blocks.NETHER_GOLD_ORE, gold);
            ORE_COLORS.put(Blocks.IRON_ORE, iron); ORE_COLORS.put(Blocks.DEEPSLATE_IRON_ORE, iron);
            ORE_COLORS.put(Blocks.COPPER_ORE, copper); ORE_COLORS.put(Blocks.DEEPSLATE_COPPER_ORE, copper);
            ORE_COLORS.put(Blocks.EMERALD_ORE, emerald); ORE_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE, emerald);
            ORE_COLORS.put(Blocks.LAPIS_ORE, lapis); ORE_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE, lapis);
            ORE_COLORS.put(Blocks.REDSTONE_ORE, redstone); ORE_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE, redstone);
            ORE_COLORS.put(Blocks.COAL_ORE, coal); ORE_COLORS.put(Blocks.DEEPSLATE_COAL_ORE, coal);
            ORE_COLORS.put(Blocks.ANCIENT_DEBRIS, debris);
            ORE_COLORS.put(Blocks.NETHER_QUARTZ_ORE, quartz);
        }

        OreScanner() { super("ore_scanner", "OreScanner", "Highlights ores in look direction (64-block ray, 3x3 cross-section)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= 10) { // Re-scan every 10 frames
                scanTick = 0;
                foundOres.clear();
                Vec3 look = mc.player.getLookAngle();
                Vec3 start = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                Set<BlockPos> visited = new HashSet<>();
                for (int dist = 1; dist <= 64; dist++) {
                    Vec3 center = start.add(look.scale(dist));
                    BlockPos centerPos = BlockPos.containing(center);
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                BlockPos pos = centerPos.offset(dx, dy, dz);
                                if (!visited.add(pos)) continue;
                                float[] color = ORE_COLORS.get(mc.level.getBlockState(pos).getBlock());
                                if (color != null) foundOres.add(new OreHit(pos, color[0], color[1], color[2]));
                            }
                        }
                    }
                }
            }
            if (foundOres.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();

                for (OreHit hit : foundOres) {
                    float x = hit.pos.getX(), y = hit.pos.getY(), z = hit.pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, hit.r, hit.g, hit.b, 0.8f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            foundOres.clear(); scanTick = 0;
        }
    }

    // ========================================================================
    // MobOwner -- Reports tamed mobs with UUID-based ownership info
    // Differs from EntityOwner: MobOwner shows mob type + UUID, EntityOwner shows detailed HP info
    // ========================================================================
    static class MobOwner extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        MobOwner() { super("mob_owner", "MobOwner", "Reports tamed mobs with type classification and owner UUID (compact format)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 64, "Scan range in blocks");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 100 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<LivingEntity> owned = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> e.isAlive() && e instanceof OwnableEntity oe && oe.getOwner() != null);
            if (!owned.isEmpty()) {
                // Compact summary: group by owner
                Map<String, List<String>> byOwner = new LinkedHashMap<>();
                for (LivingEntity animal : owned) {
                    OwnableEntity oe = (OwnableEntity) animal;
                    LivingEntity owner = oe.getOwner();
                    String ownerStr = owner != null ? owner.getName().getString() : "unknown";
                    String entityType = animal.getType().getDescription().getString();
                    String displayName = animal.hasCustomName() ? animal.getCustomName().getString() + " (" + entityType + ")" : entityType;
                    byOwner.computeIfAbsent(ownerStr, k -> new ArrayList<>()).add(displayName);
                }
                player.sendSystemMessage(Component.literal("\u00a76[MobOwner] \u00a7fTamed mobs by owner (" + owned.size() + " total):"));
                for (Map.Entry<String, List<String>> entry : byOwner.entrySet()) {
                    player.sendSystemMessage(Component.literal("\u00a77  \u00a7a" + entry.getKey() + "\u00a77: " + String.join(", ", entry.getValue())));
                }
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // AntiOverlay -- Removes screen overlays (pumpkin, water, powder snow)
    // via mixin AND removes Darkness/Blindness effects server-side
    // ========================================================================
    static class AntiOverlay extends AdminModule {
        private int tickCounter = 0;
        AntiOverlay() { super("anti_overlay", "AntiOverlay", "Removes pumpkin/water overlays (mixin) and Darkness/Blindness effects (server)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.antiOverlayEnabled = true;
        }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 5 != 0) return;
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.BLINDNESS);
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.antiOverlayEnabled = false;
            tickCounter = 0;
        }
    }

    // ========================================================================
    // Zoom -- True FOV zoom via NeoForge ComputeFov event
    // ========================================================================
    static class Zoom extends AdminModule {
        private ModuleSetting.DoubleSetting factor;
        Zoom() { super("zoom", "Zoom", "Zoom in by reducing FOV (toggle on/off)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            factor = decimal("Factor", 4.0, 1.5, 10.0, "Zoom factor (higher = more zoom)");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.zoomEnabled = true;
            AdminModuleState.zoomFactor = factor.getValue().floatValue();
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.zoomEnabled = false;
        }
    }

    // ========================================================================
    // NEW METEOR-STYLE MODULES
    // ========================================================================

    // ========================================================================
    // BetterTooltips -- Send detailed item info to chat when held item changes
    // ========================================================================
    static class BetterTooltips extends AdminModule {
        private int tickCounter = 0;
        private ItemStack lastHeldItem = ItemStack.EMPTY;

        BetterTooltips() { super("better_tooltips", "BetterTooltips", "Shows detailed item info in chat when switching held items", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }

        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 20 != 0) return;
            ItemStack held = player.getMainHandItem();
            if (held.isEmpty() && lastHeldItem.isEmpty()) return;
            if (ItemStack.isSameItemSameComponents(held, lastHeldItem)) return;
            lastHeldItem = held.copy();
            if (held.isEmpty()) return;

            StringBuilder sb = new StringBuilder("\u00a76[BetterTooltips] \u00a7f");
            sb.append(held.getHoverName().getString());
            sb.append(" \u00a77x").append(held.getCount());
            if (held.isDamageableItem()) {
                int durability = held.getMaxDamage() - held.getDamageValue();
                sb.append(" \u00a77| Durability: \u00a7a").append(durability).append("/").append(held.getMaxDamage());
            }
            if (held.isEnchanted()) {
                sb.append(" \u00a77| \u00a7dEnchanted");
            }
            String itemId = held.getItem().toString();
            sb.append(" \u00a77| ID: \u00a78").append(itemId);
            player.sendSystemMessage(Component.literal(sb.toString()));
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
            lastHeldItem = ItemStack.EMPTY;
        }
    }

    // ========================================================================
    // BreakIndicators -- Shows block breaking progress with colored outlines
    // ========================================================================
    static class BreakIndicators extends AdminModule {
        BreakIndicators() { super("break_indicators", "BreakIndicators", "Shows colored outlines around blocks being broken by any player", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            // Access the break progress map from the level renderer
            // In 1.21.11 the destroy progress is handled internally; we can check
            // if the player is currently mining and show a colored outline on the target block
            if (mc.hitResult == null || mc.hitResult.getType() != net.minecraft.world.phys.HitResult.Type.BLOCK) return;
            if (!mc.player.swinging) return;
            var blockHit = (net.minecraft.world.phys.BlockHitResult) mc.hitResult;
            BlockPos pos = blockHit.getBlockPos();
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                // Color based on approximate break progress (green -> yellow -> red)
                // We estimate by using getDestroyProgress which gives the per-tick progress rate
                float breakSpeed = mc.level.getBlockState(pos).getDestroyProgress(mc.player, mc.level, pos);
                float r = Math.min(1.0f, breakSpeed * 20);
                float g = Math.max(0.0f, 1.0f - breakSpeed * 20);
                float x1 = pos.getX() - 0.005f, y1 = pos.getY() - 0.005f, z1 = pos.getZ() - 0.005f;
                float x2 = pos.getX() + 1.005f, y2 = pos.getY() + 1.005f, z2 = pos.getZ() + 1.005f;
                // Bottom
                consumer.addVertex(matrix,x1,y1,z1).setColor(r,g,0,0.9f).setNormal(1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z1).setColor(r,g,0,0.9f).setNormal(1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z1).setColor(r,g,0,0.9f).setNormal(0,0,1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z2).setColor(r,g,0,0.9f).setNormal(0,0,1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z2).setColor(r,g,0,0.9f).setNormal(-1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y1,z2).setColor(r,g,0,0.9f).setNormal(-1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y1,z2).setColor(r,g,0,0.9f).setNormal(0,0,-1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y1,z1).setColor(r,g,0,0.9f).setNormal(0,0,-1).setLineWidth(2.0f);
                // Top
                consumer.addVertex(matrix,x1,y2,z1).setColor(r,g,0,0.9f).setNormal(1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z1).setColor(r,g,0,0.9f).setNormal(1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z1).setColor(r,g,0,0.9f).setNormal(0,0,1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z2).setColor(r,g,0,0.9f).setNormal(0,0,1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z2).setColor(r,g,0,0.9f).setNormal(-1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y2,z2).setColor(r,g,0,0.9f).setNormal(-1,0,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y2,z2).setColor(r,g,0,0.9f).setNormal(0,0,-1).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y2,z1).setColor(r,g,0,0.9f).setNormal(0,0,-1).setLineWidth(2.0f);
                // Verticals
                consumer.addVertex(matrix,x1,y1,z1).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y2,z1).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z1).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z1).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y1,z2).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x2,y2,z2).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y1,z2).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                consumer.addVertex(matrix,x1,y2,z2).setColor(r,g,0,0.9f).setNormal(0,1,0).setLineWidth(2.0f);
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
    }

    // ========================================================================
    // PopChams -- Draws a flash wireframe when entities use totems
    // ========================================================================
    static class PopChams extends AdminModule {
        private final Map<Integer, Long> popTimes = new HashMap<>();
        PopChams() { super("pop_chams", "PopChams", "Draws a bright wireframe flash when entities use a totem of undying", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        /** Called to record a totem pop. Entity ID tracked for 2 seconds of glow. */
        public void recordPop(int entityId) {
            popTimes.put(entityId, System.currentTimeMillis());
        }
        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;
            if (popTimes.isEmpty()) return;
            // Clean expired entries
            long now = System.currentTimeMillis();
            popTimes.entrySet().removeIf(e -> now - e.getValue() > 2000);
            if (popTimes.isEmpty()) return;
            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                for (Map.Entry<Integer, Long> entry : popTimes.entrySet()) {
                    Entity entity = mc.level.getEntity(entry.getKey());
                    if (entity == null || !entity.isAlive()) continue;
                    float elapsed = (now - entry.getValue()) / 2000.0f; // 0 to 1 over 2 seconds
                    float alpha = 1.0f - elapsed;
                    // Expand the box slightly over time for a flash effect
                    float expand = elapsed * 0.3f;
                    net.minecraft.world.phys.AABB box = entity.getBoundingBox().inflate(expand);
                    // Gold/white flash color
                    float cr = 1.0f, cg = 0.84f + elapsed * 0.16f, cb = 0.0f + elapsed;
                    // Draw wireframe box (bottom, top, verticals)
                    float x1=(float)box.minX, y1=(float)box.minY, z1=(float)box.minZ;
                    float x2=(float)box.maxX, y2=(float)box.maxY, z2=(float)box.maxZ;
                    consumer.addVertex(matrix,x1,y1,z1).setColor(cr,cg,cb,alpha).setNormal(1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z1).setColor(cr,cg,cb,alpha).setNormal(1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z1).setColor(cr,cg,cb,alpha).setNormal(0,0,1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z2).setColor(cr,cg,cb,alpha).setNormal(0,0,1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z2).setColor(cr,cg,cb,alpha).setNormal(-1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y1,z2).setColor(cr,cg,cb,alpha).setNormal(-1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y1,z2).setColor(cr,cg,cb,alpha).setNormal(0,0,-1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y1,z1).setColor(cr,cg,cb,alpha).setNormal(0,0,-1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z1).setColor(cr,cg,cb,alpha).setNormal(1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z1).setColor(cr,cg,cb,alpha).setNormal(1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z1).setColor(cr,cg,cb,alpha).setNormal(0,0,1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z2).setColor(cr,cg,cb,alpha).setNormal(0,0,1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z2).setColor(cr,cg,cb,alpha).setNormal(-1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z2).setColor(cr,cg,cb,alpha).setNormal(-1,0,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z2).setColor(cr,cg,cb,alpha).setNormal(0,0,-1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z1).setColor(cr,cg,cb,alpha).setNormal(0,0,-1).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y1,z1).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z1).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z1).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z1).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y1,z2).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x2,y2,z2).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y1,z2).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                    consumer.addVertex(matrix,x1,y2,z2).setColor(cr,cg,cb,alpha).setNormal(0,1,0).setLineWidth(2.0f);
                }
                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }
    }

    // ========================================================================
    // WallHack -- Apply Glowing to PLAYERS ONLY to see them through walls
    // Differs from Chams: WallHack targets players only, Chams targets all entities
    // ========================================================================
    static class WallHack extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        WallHack() { super("wall_hack", "WallHack", "Glowing on PLAYERS ONLY to see them through walls (use Chams for all entities)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 64, 8, 128, "Effect range in blocks");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 20 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<Player> players = level.getEntitiesOfClass(Player.class, box, e -> e.isAlive() && e != player);
            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // Trail -- Leaves SOUL_FIRE_FLAME particle trail behind you (server-side)
    // Differs from Breadcrumbs: Trail uses fiery SOUL_FIRE_FLAME particles, Breadcrumbs uses soft END_ROD
    // ========================================================================
    static class Trail extends AdminModule {
        private ModuleSetting.IntSetting interval;
        private ModuleSetting.EnumSetting particleType;
        private int tick = 0;
        Trail() { super("trail", "Trail", "Leaves a fiery particle trail (SOUL_FIRE_FLAME) behind you for style", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            interval = integer("Interval", 5, 1, 20, "Ticks between trail particles");
            particleType = enumVal("Particle", "Soul Flame", List.of("Soul Flame", "Flame", "Heart", "Note", "Enchant"), "Trail particle style");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            if (++tick % interval.getValue() != 0) return;
            var particle = switch (particleType.getValue()) {
                case "Flame" -> net.minecraft.core.particles.ParticleTypes.FLAME;
                case "Heart" -> net.minecraft.core.particles.ParticleTypes.HEART;
                case "Note" -> net.minecraft.core.particles.ParticleTypes.NOTE;
                case "Enchant" -> net.minecraft.core.particles.ParticleTypes.ENCHANT;
                default -> net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME;
            };
            level.sendParticles((net.minecraft.core.particles.ParticleOptions) particle,
                player.getX(), player.getY() + 0.1, player.getZ(), 2, 0.1, 0, 0.1, 0.01);
        }
        @Override public void onDisable(ServerPlayer player) {
            tick = 0;
        }
    }

    // ========================================================================
    // Marker -- Place a glowstone block at player's position as a marker
    // ========================================================================
    static class Marker extends AdminModule {
        Marker() { super("marker", "Marker", "Places a glowstone block at your position as a one-shot marker", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onEnable(ServerPlayer player) {
            ServerLevel level = (ServerLevel) player.level();
            BlockPos pos = player.blockPosition();
            // Place glowstone one block above feet position so we don't trap the player
            BlockPos markerPos = pos.above(2);
            if (level.getBlockState(markerPos).isAir()) {
                level.setBlock(markerPos, Blocks.GLOWSTONE.defaultBlockState(), 3);
                player.sendSystemMessage(Component.literal("\u00a76[Marker] \u00a7fPlaced glowstone marker at \u00a7e"
                        + markerPos.getX() + ", " + markerPos.getY() + ", " + markerPos.getZ()));
            } else {
                player.sendSystemMessage(Component.literal("\u00a76[Marker] \u00a7cBlock above is not air, marker not placed"));
            }
        }
    }

    // ========================================================================
    // BetterTab -- Enhanced tab list with ping colors and health (via mixin)
    // ========================================================================
    static class BetterTab extends AdminModule {
        BetterTab() { super("better_tab", "BetterTab", "Tab list shows ping latency with colors and player health", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.betterTabEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.betterTabEnabled = false;
        }
    }

    // ========================================================================
    // Xray -- Client-side ESP: highlights ores, containers, and rare blocks
    // through walls with color-coded wireframes. Auto-enables fullbright.
    // Meteor Client-inspired: client-side scanning + no-depth-test rendering.
    // ========================================================================
    static class Xray extends AdminModule {
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.IntSetting scanRate;
        private ModuleSetting.BoolSetting showOres;
        private ModuleSetting.BoolSetting showContainers;
        private ModuleSetting.BoolSetting showRare;

        private final List<XrayPos> foundBlocks = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<XrayPos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        private record XrayPos(BlockPos pos, float r, float g, float b) {}

        private static final Map<Block, float[]> ORE_COLORS = new HashMap<>();
        private static final Map<Block, float[]> CONTAINER_COLORS = new HashMap<>();
        private static final Map<Block, float[]> RARE_COLORS = new HashMap<>();

        static {
            float[] diamond = {0.2f, 0.9f, 0.9f}, gold = {1.0f, 0.85f, 0.0f};
            float[] iron = {0.85f, 0.7f, 0.6f}, copper = {0.9f, 0.55f, 0.3f};
            float[] emerald = {0.2f, 1.0f, 0.2f}, lapis = {0.2f, 0.3f, 1.0f};
            float[] redstone = {1.0f, 0.0f, 0.0f}, coal = {0.4f, 0.4f, 0.4f};
            float[] quartz = {1.0f, 1.0f, 0.9f};
            ORE_COLORS.put(Blocks.DIAMOND_ORE, diamond); ORE_COLORS.put(Blocks.DEEPSLATE_DIAMOND_ORE, diamond);
            ORE_COLORS.put(Blocks.GOLD_ORE, gold); ORE_COLORS.put(Blocks.DEEPSLATE_GOLD_ORE, gold); ORE_COLORS.put(Blocks.NETHER_GOLD_ORE, gold);
            ORE_COLORS.put(Blocks.IRON_ORE, iron); ORE_COLORS.put(Blocks.DEEPSLATE_IRON_ORE, iron);
            ORE_COLORS.put(Blocks.COPPER_ORE, copper); ORE_COLORS.put(Blocks.DEEPSLATE_COPPER_ORE, copper);
            ORE_COLORS.put(Blocks.EMERALD_ORE, emerald); ORE_COLORS.put(Blocks.DEEPSLATE_EMERALD_ORE, emerald);
            ORE_COLORS.put(Blocks.LAPIS_ORE, lapis); ORE_COLORS.put(Blocks.DEEPSLATE_LAPIS_ORE, lapis);
            ORE_COLORS.put(Blocks.REDSTONE_ORE, redstone); ORE_COLORS.put(Blocks.DEEPSLATE_REDSTONE_ORE, redstone);
            ORE_COLORS.put(Blocks.COAL_ORE, coal); ORE_COLORS.put(Blocks.DEEPSLATE_COAL_ORE, coal);
            ORE_COLORS.put(Blocks.NETHER_QUARTZ_ORE, quartz);
            CONTAINER_COLORS.put(Blocks.CHEST, new float[]{1.0f, 0.8f, 0.0f});
            CONTAINER_COLORS.put(Blocks.TRAPPED_CHEST, new float[]{1.0f, 0.6f, 0.0f});
            CONTAINER_COLORS.put(Blocks.BARREL, new float[]{0.7f, 0.5f, 0.2f});
            CONTAINER_COLORS.put(Blocks.ENDER_CHEST, new float[]{0.0f, 0.7f, 0.7f});
            RARE_COLORS.put(Blocks.ANCIENT_DEBRIS, new float[]{0.6f, 0.3f, 0.2f});
            RARE_COLORS.put(Blocks.SPAWNER, new float[]{1.0f, 0.0f, 0.5f});
            RARE_COLORS.put(Blocks.DIAMOND_BLOCK, new float[]{0.4f, 1.0f, 1.0f});
            RARE_COLORS.put(Blocks.EMERALD_BLOCK, new float[]{0.3f, 1.0f, 0.3f});
            RARE_COLORS.put(Blocks.GOLD_BLOCK, new float[]{1.0f, 0.9f, 0.2f});
        }

        Xray() { super("xray", "Xray", "Highlights ores, containers, and rare blocks through walls with colored wireframes + fullbright", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            radius = integer("Radius", 32, 8, 48, "Scan radius in blocks");
            scanRate = integer("Scan Rate", 2, 1, 20, "Frames between scan slices (lower = faster updates)");
            showOres = bool("Ores", true, "Highlight ore blocks");
            showContainers = bool("Containers", true, "Highlight chests, barrels, shulkers, ender chests");
            showRare = bool("Rare", true, "Highlight ancient debris, spawners, gem blocks");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.xrayEnabled = true;
            AdminModuleState.fullbrightGammaEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.xrayEnabled = false;
            AdminModuleState.fullbrightGammaEnabled = false;
            foundBlocks.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            // Incremental scan: a few Y-layers per frame
            scanTick++;
            if (scanTick >= scanRate.getValue()) {
                scanTick = 0;
                xrayScanIncremental(mc);
            }
            if (foundBlocks.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);

                // Use see-through render type — visible through all blocks
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();

                for (XrayPos hp : foundBlocks) {
                    float x = hp.pos.getX(), y = hp.pos.getY(), z = hp.pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, hp.r, hp.g, hp.b, 0.8f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void xrayScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = radius.getValue();
            BlockPos center = mc.player.blockPosition();
            int slicesPerTick = 4;
            if (!scanInProgress) {
                scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true;
            }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        var state = mc.level.getBlockState(pos);
                        float[] color = xrayGetColor(state);
                        if (color != null) scanBuffer.add(new XrayPos(pos, color[0], color[1], color[2]));
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                foundBlocks.clear(); foundBlocks.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        private float[] xrayGetColor(net.minecraft.world.level.block.state.BlockState state) {
            Block block = state.getBlock();
            if (showOres.getValue()) { float[] c = ORE_COLORS.get(block); if (c != null) return c; }
            if (showRare.getValue()) { float[] c = RARE_COLORS.get(block); if (c != null) return c; }
            if (showContainers.getValue()) {
                float[] c = CONTAINER_COLORS.get(block); if (c != null) return c;
                if (state.is(net.minecraft.tags.BlockTags.SHULKER_BOXES)) return new float[]{0.8f, 0.3f, 0.8f};
            }
            return null;
        }
    }

    // ========================================================================
    // NEW METEOR-STYLE MODULES (BATCH 2)
    // ========================================================================

    // ========================================================================
    // BlockESP -- Scan for configurable block types, report positions via chat
    //             and apply Glowing to entities near those blocks
    // ========================================================================
    static class BlockESP extends AdminModule {
        private ModuleSetting.EnumSetting blockType;
        private ModuleSetting.IntSetting radius;
        private ModuleSetting.IntSetting scanRate;
        private ModuleSetting.EnumSetting color;

        private final List<BlockPos> foundBlocks = new ArrayList<>();
        private int scanTick = 0;
        private int currentScanSlice = 0;
        private final List<BlockPos> scanBuffer = new ArrayList<>();
        private boolean scanInProgress = false;

        private static final List<String> COLOR_OPTIONS = List.of("Red", "Blue", "Green", "Yellow", "Magenta", "Cyan", "White");

        BlockESP() { super("block_esp", "BlockESP", "Highlights specific blocks through walls with colored wireframes", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            blockType = enumVal("Block", "Spawner", List.of("Spawner", "Chest", "EndPortalFrame", "Beacon", "NetherPortal", "EndPortal"), "Block type to scan for");
            radius = integer("Radius", 32, 8, 48, "Scan radius in blocks");
            scanRate = integer("Scan Rate", 4, 1, 20, "Frames between scan slices");
            color = enumVal("Color", "Magenta", COLOR_OPTIONS, "Wireframe color");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }

        private Set<Block> getTargetBlocks() {
            return switch (blockType.getValue()) {
                case "Chest" -> Set.of(Blocks.CHEST, Blocks.TRAPPED_CHEST);
                case "EndPortalFrame" -> Set.of(Blocks.END_PORTAL_FRAME);
                case "Beacon" -> Set.of(Blocks.BEACON);
                case "NetherPortal" -> Set.of(Blocks.NETHER_PORTAL);
                case "EndPortal" -> Set.of(Blocks.END_PORTAL);
                default -> Set.of(Blocks.SPAWNER);
            };
        }

        private float[] getColorRGB() {
            return switch (color.getValue()) {
                case "Red" -> new float[]{1.0f, 0.2f, 0.2f};
                case "Blue" -> new float[]{0.2f, 0.4f, 1.0f};
                case "Green" -> new float[]{0.2f, 1.0f, 0.2f};
                case "Yellow" -> new float[]{1.0f, 1.0f, 0.2f};
                case "Cyan" -> new float[]{0.0f, 0.9f, 0.9f};
                case "White" -> new float[]{1.0f, 1.0f, 1.0f};
                default -> new float[]{1.0f, 0.0f, 0.8f}; // Magenta
            };
        }

        @Override public void onRenderWorld(Object eventObj) {
            if (!(eventObj instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterTranslucentBlocks event)) return;
            var mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            scanTick++;
            if (scanTick >= scanRate.getValue()) {
                scanTick = 0;
                blockEspScanIncremental(mc);
            }
            if (foundBlocks.isEmpty()) return;

            var poseStack = event.getPoseStack();
            try {
                Vec3 camPos = mc.player.getEyePosition(mc.getDeltaTracker().getRealtimeDeltaTicks());
                var bufferSource = mc.renderBuffers().bufferSource();
                poseStack.pushPose();
                poseStack.translate(-camPos.x, -camPos.y, -camPos.z);
                var consumer = bufferSource.getBuffer(ESPRenderHelper.LINES_SEE_THROUGH);
                var matrix = poseStack.last().pose();
                float[] rgb = getColorRGB();

                for (BlockPos pos : foundBlocks) {
                    float x = pos.getX(), y = pos.getY(), z = pos.getZ();
                    ESPRenderHelper.drawWireBox(consumer, matrix, x, y, z, x + 1, y + 1, z + 1, rgb[0], rgb[1], rgb[2], 0.8f);
                }

                poseStack.popPose();
                bufferSource.endBatch();
            } catch (Exception e) {
                try { poseStack.popPose(); } catch (Exception ignored) {}
            }
        }

        private void blockEspScanIncremental(net.minecraft.client.Minecraft mc) {
            int r = radius.getValue();
            BlockPos center = mc.player.blockPosition();
            Set<Block> targets = getTargetBlocks();
            int slicesPerTick = 4;
            if (!scanInProgress) {
                scanBuffer.clear(); currentScanSlice = -r; scanInProgress = true;
            }
            int slicesDone = 0;
            while (currentScanSlice <= r && slicesDone < slicesPerTick) {
                int y = currentScanSlice;
                for (int x = -r; x <= r; x++) {
                    for (int z = -r; z <= r; z++) {
                        BlockPos pos = center.offset(x, y, z);
                        if (targets.contains(mc.level.getBlockState(pos).getBlock())) {
                            scanBuffer.add(pos);
                        }
                    }
                }
                currentScanSlice++; slicesDone++;
            }
            if (currentScanSlice > r) {
                foundBlocks.clear(); foundBlocks.addAll(scanBuffer);
                scanBuffer.clear(); scanInProgress = false;
            }
        }

        @Override public void onDisable(ServerPlayer player) {
            foundBlocks.clear(); scanBuffer.clear();
            scanInProgress = false; scanTick = 0;
        }
    }

    // ========================================================================
    // BlockSelection -- Client stub: custom block selection box color
    // ========================================================================
    static class BlockSelection extends AdminModule {
        BlockSelection() { super("block_selection", "BlockSelection", "Custom block selection box color (client-side, uses BlockHighlight state)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.blockHighlightEnabled = true;
            AdminModuleState.blockHighlightR = 0.0f;
            AdminModuleState.blockHighlightG = 1.0f;
            AdminModuleState.blockHighlightB = 0.5f;
            AdminModuleState.blockHighlightA = 0.8f;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.blockHighlightEnabled = false;
        }
    }

    // ========================================================================
    // Blur -- Client stub: GUI blur effect (description only, needs shader mixin)
    // ========================================================================
    static class Blur extends AdminModule {
        Blur() { super("blur", "Blur", "Blurs the background when GUIs are open (client-side shader, description only)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        // No-op: would require shader mixin to implement properly
    }

    // ========================================================================
    // BossStack -- Remove wither-related effects to reduce boss bar clutter
    // ========================================================================
    static class BossStack extends AdminModule {
        private int tickCounter = 0;
        BossStack() { super("boss_stack", "BossStack", "Removes Wither and related boss effects to reduce visual clutter", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 20 != 0) return;
            // Remove wither-related effects that bosses inflict
            player.removeEffect(MobEffects.WITHER);
            player.removeEffect(MobEffects.DARKNESS);
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
        }
    }

    // ========================================================================
    // CameraTweaks -- Client-side camera adjustments via AdminModuleState
    // ========================================================================
    static class CameraTweaks extends AdminModule {
        private ModuleSetting.DoubleSetting distance;
        CameraTweaks() { super("camera_tweaks", "CameraTweaks", "Adjusts third-person camera distance (client-side state)", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            distance = decimal("Distance", 6.0, 2.0, 20.0, "Third-person camera distance");
        }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.cameraTweaksEnabled = true;
            AdminModuleState.cameraTweaksDistance = distance.getValue().floatValue();
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.cameraTweaksEnabled = false;
        }
    }

    // ========================================================================
    // ItemHighlight -- Apply Glowing to item entities near the player
    // ========================================================================
    static class ItemHighlight extends AdminModule {
        private int tickCounter = 0;
        private ModuleSetting.IntSetting range;
        ItemHighlight() { super("item_highlight", "ItemHighlight", "Apply Glowing to dropped item entities for easy visibility", ModuleCategory.RENDER); }
        @Override protected void initSettings() {
            range = integer("Range", 16, 4, 32, "Item highlight range");
        }
        @Override public boolean isServerSide() { return true; }
        @Override public boolean isClientSide() { return false; }
        @Override public void onServerTick(ServerPlayer player, ServerLevel level) {
            tickCounter++;
            if (tickCounter % 20 != 0) return;
            int r = range.getValue();
            AABB box = player.getBoundingBox().inflate(r);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, Entity::isAlive);
            for (ItemEntity item : items) {
                item.setGlowingTag(true);
            }
        }
        @Override public void onDisable(ServerPlayer player) {
            tickCounter = 0;
            // Remove glow from nearby items
            ServerLevel level = (ServerLevel) player.level();
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(32), Entity::isAlive);
            for (ItemEntity item : items) {
                item.setGlowingTag(false);
            }
        }
    }

    // ========================================================================
    // NoFog -- Disable all fog rendering via client mixin (FogRendererMixin)
    // ========================================================================
    static class NoFog extends AdminModule {
        NoFog() { super("no_fog", "NoFog", "Disables distance/water/lava/blindness fog rendering", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.noFogEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.noFogEnabled = false;
        }
    }

    // ========================================================================
    // ContainerButtons -- Steal/Dump buttons on container screens (AbstractContainerScreenMixin)
    // Press T to steal all items from container, G to dump all into container
    // ========================================================================
    static class ContainerButtons extends AdminModule {
        ContainerButtons() { super("container_buttons", "ContainerButtons", "Adds [T] Steal All and [G] Dump All to container screens", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.containerButtonsEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.containerButtonsEnabled = false;
        }
    }

    // ========================================================================
    // AttackUseTick -- Track mouse button state for combat/player modules (MinecraftMixin)
    // Enables attackTickActive / useTickActive flags that other modules can read
    // ========================================================================
    static class AttackUseTick extends AdminModule {
        AttackUseTick() { super("attack_use_tick", "AttackUseTick", "Tracks mouse click state for combat modules (attackTickActive/useTickActive flags)", ModuleCategory.RENDER); }
        @Override public boolean isServerSide() { return false; }
        @Override public boolean isClientSide() { return true; }
        @Override public void onEnable(ServerPlayer player) {
            AdminModuleState.attackTickEnabled = true;
            AdminModuleState.useTickEnabled = true;
        }
        @Override public void onDisable(ServerPlayer player) {
            AdminModuleState.attackTickEnabled = false;
            AdminModuleState.useTickEnabled = false;
            AdminModuleState.attackTickActive = false;
            AdminModuleState.useTickActive = false;
        }
    }
}
