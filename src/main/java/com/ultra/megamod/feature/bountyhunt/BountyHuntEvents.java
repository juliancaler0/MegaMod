package com.ultra.megamod.feature.bountyhunt;

import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Event handlers for the Bounty Hunting system.
 * - Designates bounty target mobs on spawn
 * - Awards bounties on kill
 * - Daily rotation and periodic save via server tick
 */
@EventBusSubscriber(modid = "megamod")
public class BountyHuntEvents {

    private static final String BOUNTY_ID_TAG = "megamod_bounty_id";
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    /**
     * When a hostile mob spawns in a non-pocket-dimension ServerLevel, check if any online
     * player has an active bounty for that mob type. If so, 2% chance to designate it
     * as a bounty target with custom name, glowing, and persistence.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Monster mob)) return;

        ServerLevel level = (ServerLevel) event.getLevel();

        // Skip pocket dimensions (dungeon, museum, casino, resource)
        String dimPath = level.dimension().identifier().getPath();
        if (dimPath.contains("dungeon") || dimPath.contains("museum")
                || dimPath.contains("casino") || dimPath.contains("pocket")
                || dimPath.contains("resource")) {
            return;
        }

        // Skip if already a bounty target
        if (mob.getPersistentData().getIntOr(BOUNTY_ID_TAG, 0) != 0) return;

        // Get the mob type path name (e.g., "zombie", "skeleton")
        String mobType = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();

        // Check if any player has an active bounty for this mob type
        BountyHuntManager.ensureLoaded(level);
        List<Map.Entry<UUID, BountyHuntManager.ActiveBounty>> matchingPlayers =
                BountyHuntManager.findPlayersWithBountyForMob(mobType);

        if (matchingPlayers.isEmpty()) return;

        // 2% chance to become a bounty target
        if (RANDOM.nextDouble() > 0.02) return;

        // Pick the first matching player/bounty pair
        Map.Entry<UUID, BountyHuntManager.ActiveBounty> match = matchingPlayers.get(
                RANDOM.nextInt(matchingPlayers.size()));
        BountyHuntManager.ActiveBounty activeBounty = match.getValue();

        // Designate as bounty target
        mob.getPersistentData().putInt(BOUNTY_ID_TAG, activeBounty.bountyId);
        mob.setCustomName(Component.literal(activeBounty.targetName).withStyle(ChatFormatting.GOLD));
        mob.setCustomNameVisible(true);
        mob.setGlowingTag(true);
        mob.setPersistenceRequired();
    }

    /**
     * When a mob with the bounty tag is killed by a ServerPlayer, check if the killer
     * has an active bounty matching that ID. If so, complete the bounty and award coins.
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity killed = event.getEntity();
        if (killed.level().isClientSide()) return;
        if (!(killed instanceof Mob mob)) return;

        // Check for bounty tag
        int bountyId = mob.getPersistentData().getIntOr(BOUNTY_ID_TAG, 0);
        if (bountyId == 0) return;

        // Check if killed by a player
        Entity source = event.getSource().getEntity();
        if (!(source instanceof ServerPlayer player)) return;

        ServerLevel level = (ServerLevel) player.level();
        BountyHuntManager.ensureLoaded(level);

        // Check if this player has an active bounty with that ID
        List<BountyHuntManager.ActiveBounty> actives = BountyHuntManager.getActiveBounties(player.getUUID());
        boolean hasMatchingBounty = false;
        for (BountyHuntManager.ActiveBounty ab : actives) {
            if (ab.bountyId == bountyId && !ab.completed) {
                hasMatchingBounty = true;
                break;
            }
        }

        if (!hasMatchingBounty) return;

        // Complete the bounty and award coins
        int reward = BountyHuntManager.completeBounty(player.getUUID(), bountyId);
        if (reward > 0) {
            com.ultra.megamod.feature.quests.QuestEventListener.onBountyComplete(player.getUUID(), level);
            EconomyManager eco = EconomyManager.get(level);
            eco.addWallet(player.getUUID(), reward);

            // Notify player
            player.sendSystemMessage(Component.literal("Bounty Complete! +" + reward + " MegaCoins")
                    .withStyle(ChatFormatting.GOLD));

            // Play success sound (totem sound for dramatic effect)
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.6f, 1.2f);

            // Save
            BountyHuntManager.saveToDisk(level);
        }
    }

    /**
     * Server tick handler for daily rotation check and periodic save.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        tickCounter++;

        // Every 1200 ticks (60 seconds): check daily rotation, clean expired, save
        if (tickCounter >= 1200) {
            tickCounter = 0;

            ServerLevel overworld = event.getServer().overworld();
            if (overworld == null) return;

            BountyHuntManager.ensureLoaded(overworld);
            BountyHuntManager.checkDailyRotation();
            BountyHuntManager.cleanAllExpired();

            if (BountyHuntManager.isDirty()) {
                BountyHuntManager.saveToDisk(overworld);
            }
        }
    }
}
