package com.ultra.megamod.feature.computer.network.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.ComputerDataPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * Server-side handler for mob showcase spawning — spawns NoAI entities in a row
 * facing the admin player for model inspection.
 */
public class MobShowcaseHandler {

    private static final String[] BOSS_IDS = {
        "megamod:wraith_boss", "megamod:ossukage_boss", "megamod:dungeon_keeper",
        "megamod:frostmaw_boss", "megamod:wroughtnaut_boss", "megamod:umvuthi_boss",
        "megamod:chaos_spawner_boss", "megamod:sculptor_boss"
    };

    private static final String[] MOB_IDS = {
        "megamod:dungeon_mob", "megamod:minion", "megamod:dungeon_rat",
        "megamod:undead_knight", "megamod:dungeon_slime", "megamod:hollow",
        "megamod:naga", "megamod:grottol", "megamod:lantern", "megamod:foliaath",
        "megamod:umvuthana", "megamod:spawner_carrier", "megamod:bluff", "megamod:baby_foliaath"
    };

    public static boolean handle(ServerPlayer player, String action, String jsonData, ServerLevel level, EconomyManager eco) {
        if (!AdminSystem.isAdmin(player)) return false;

        switch (action) {
            case "mob_showcase_spawn" -> {
                handleSpawn(player, jsonData, level, eco);
                return true;
            }
            case "mob_showcase_kill" -> {
                handleKill(player, level, eco);
                return true;
            }
        }
        return false;
    }

    private static void handleSpawn(ServerPlayer player, String jsonData, ServerLevel level, EconomyManager eco) {
        try {
            JsonObject obj = JsonParser.parseString(jsonData).getAsJsonObject();
            String type = obj.get("type").getAsString();

            // Get player's look direction for spacing (perpendicular to look)
            Vec3 look = player.getLookAngle();
            Vec3 pos = player.position();

            // Spawn direction: in front of player
            double frontX = look.x;
            double frontZ = look.z;
            double len = Math.sqrt(frontX * frontX + frontZ * frontZ);
            if (len < 0.01) { frontX = 0; frontZ = 1; len = 1; }
            frontX /= len;
            frontZ /= len;

            // Right vector (perpendicular)
            double rightX = -frontZ;
            double rightZ = frontX;

            int spawned = 0;

            switch (type) {
                case "single" -> {
                    String entityId = obj.get("entity").getAsString();
                    double spawnX = pos.x + frontX * 5;
                    double spawnZ = pos.z + frontZ * 5;
                    if (spawnNoAI(level, entityId, spawnX, pos.y, spawnZ, player)) {
                        spawned = 1;
                    }
                }
                case "all_bosses" -> {
                    spawned = spawnRow(level, BOSS_IDS, pos, frontX, frontZ, rightX, rightZ, player);
                }
                case "all_mobs" -> {
                    spawned = spawnRow(level, MOB_IDS, pos, frontX, frontZ, rightX, rightZ, player);
                }
                case "all" -> {
                    // Bosses in front row, mobs in back row
                    int bossCount = spawnRow(level, BOSS_IDS, pos, frontX, frontZ, rightX, rightZ, player);
                    // Offset mobs further back
                    double offsetX = pos.x + frontX * 10;
                    double offsetZ = pos.z + frontZ * 10;
                    Vec3 offsetPos = new Vec3(offsetX, pos.y, offsetZ);
                    int mobCount = spawnRowFromPos(level, MOB_IDS, offsetPos, rightX, rightZ, player);
                    spawned = bossCount + mobCount;
                }
            }

            sendResult(player, "Spawned " + spawned + " showcase entities (NoAI)", eco);
        } catch (Exception e) {
            sendResult(player, "Error: " + e.getMessage(), eco);
        }
    }

    private static int spawnRow(ServerLevel level, String[] entityIds, Vec3 playerPos,
                                 double frontX, double frontZ, double rightX, double rightZ,
                                 ServerPlayer player) {
        int spawned = 0;
        int count = entityIds.length;
        double startOffset = -(count - 1) * 2.0; // 4 blocks apart, centered

        for (int i = 0; i < count; i++) {
            double lateralOffset = startOffset + i * 4.0;
            double spawnX = playerPos.x + frontX * 8 + rightX * lateralOffset;
            double spawnZ = playerPos.z + frontZ * 8 + rightZ * lateralOffset;
            if (spawnNoAI(level, entityIds[i], spawnX, playerPos.y, spawnZ, player)) {
                spawned++;
            }
        }
        return spawned;
    }

    private static int spawnRowFromPos(ServerLevel level, String[] entityIds, Vec3 center,
                                        double rightX, double rightZ, ServerPlayer player) {
        int spawned = 0;
        int count = entityIds.length;
        double startOffset = -(count - 1) * 2.0;

        for (int i = 0; i < count; i++) {
            double lateralOffset = startOffset + i * 4.0;
            double spawnX = center.x + rightX * lateralOffset;
            double spawnZ = center.z + rightZ * lateralOffset;
            if (spawnNoAI(level, entityIds[i], spawnX, center.y, spawnZ, player)) {
                spawned++;
            }
        }
        return spawned;
    }

    /**
     * Spawn a single NoAI entity at the given position, facing the player.
     */
    private static boolean spawnNoAI(ServerLevel level, String entityId, double x, double y, double z, ServerPlayer player) {
        try {
            Identifier id = Identifier.parse(entityId);
            Optional<EntityType<?>> optType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
            if (optType.isEmpty()) return false;

            EntityType<?> entityType = optType.get();
            Entity entity = entityType.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (entity == null) return false;

            entity.setPos(x, y, z);

            // Face towards the player
            double dx = player.getX() - x;
            double dz = player.getZ() - z;
            float yaw = (float) (Math.atan2(-dx, dz) * (180.0 / Math.PI));
            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);

            // Set NoAI
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setPersistenceRequired();
                mob.setYBodyRot(yaw);
            }

            // Tag for easy cleanup
            entity.addTag("megamod_showcase");

            level.addFreshEntity(entity);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleKill(ServerPlayer player, ServerLevel level, EconomyManager eco) {
        // Kill all entities with the showcase tag within 100 blocks
        AABB area = new AABB(player.blockPosition()).inflate(100);
        List<Entity> entities = level.getEntities((Entity) null, area,
            e -> e.getTags().contains("megamod_showcase"));

        int killed = 0;
        for (Entity entity : entities) {
            entity.discard();
            killed++;
        }
        sendResult(player, "Removed " + killed + " showcase entities", eco);
    }

    private static void sendResult(ServerPlayer player, String message, EconomyManager eco) {
        JsonObject obj = new JsonObject();
        obj.addProperty("msg", message);
        ComputerDataPayload response = new ComputerDataPayload(
            "mob_showcase_result", obj.toString(),
            eco.getWallet(player.getUUID()),
            eco.getBank(player.getUUID())
        );
        PacketDistributor.sendToPlayer(player, (CustomPacketPayload) response, (CustomPacketPayload[]) new CustomPacketPayload[0]);
    }
}
