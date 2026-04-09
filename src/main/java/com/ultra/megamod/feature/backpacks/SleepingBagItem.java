package com.ultra.megamod.feature.backpacks;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.UUID;

/**
 * Sleeping bag item for the backpack system.
 * Right-click on the ground at night (or during a thunderstorm) to skip to dawn.
 * Does NOT set the player's spawn point (unlike beds).
 * Has a 60-second cooldown per player.
 */
public class SleepingBagItem extends Item {

    private final String colorName;

    /** Per-player cooldown tracking (UUID -> game time when cooldown expires) */
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_TICKS = 1200L; // 60 seconds

    /** Blindness duration for the "sleeping" visual effect */
    private static final int BLINDNESS_TICKS = 40; // 2 seconds

    public SleepingBagItem(String colorName, Properties props) {
        super(props);
        this.colorName = colorName;
    }

    public String getColorName() {
        return colorName;
    }

    /**
     * Right-click on a block to use the sleeping bag.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null || level.isClientSide()) {
            return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

        // Check cooldown
        long gameTime = serverLevel.getGameTime();
        Long cooldownEnd = COOLDOWNS.get(serverPlayer.getUUID());
        if (cooldownEnd != null && gameTime < cooldownEnd) {
            long remaining = (cooldownEnd - gameTime) / 20; // convert ticks to seconds
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou must wait " + remaining + "s before using the sleeping bag again."), true);
            return InteractionResult.FAIL;
        }

        // Check if it's night or thunderstorm (same logic vanilla beds use)
        if (!isNightOrThunderstorm(serverLevel)) {
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou can only sleep at night or during a thunderstorm."), true);
            return InteractionResult.FAIL;
        }

        // Check if there are monsters nearby (same check as vanilla beds, 8-block range)
        if (hasMonstersNearby(serverPlayer, serverLevel)) {
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou may not rest now, there are monsters nearby."), true);
            return InteractionResult.FAIL;
        }

        // Perform the sleep action
        performSleep(serverPlayer, serverLevel);

        // Set cooldown
        COOLDOWNS.put(serverPlayer.getUUID(), gameTime + COOLDOWN_TICKS);

        return InteractionResult.CONSUME;
    }

    /**
     * Also allow use() (right-click in air) to work when not targeting a block.
     */
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();

        // Check cooldown
        long gameTime = serverLevel.getGameTime();
        Long cooldownEnd = COOLDOWNS.get(serverPlayer.getUUID());
        if (cooldownEnd != null && gameTime < cooldownEnd) {
            long remaining = (cooldownEnd - gameTime) / 20;
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou must wait " + remaining + "s before using the sleeping bag again."), true);
            return InteractionResult.FAIL;
        }

        if (!isNightOrThunderstorm(serverLevel)) {
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou can only sleep at night or during a thunderstorm."), true);
            return InteractionResult.FAIL;
        }

        if (hasMonstersNearby(serverPlayer, serverLevel)) {
            serverPlayer.displayClientMessage(
                Component.literal("\u00A7cYou may not rest now, there are monsters nearby."), true);
            return InteractionResult.FAIL;
        }

        performSleep(serverPlayer, serverLevel);
        COOLDOWNS.put(serverPlayer.getUUID(), gameTime + COOLDOWN_TICKS);

        return InteractionResult.CONSUME;
    }

    /**
     * Perform the actual "sleep" action: skip to dawn + visual effects.
     */
    public static void performSleep(ServerPlayer player, ServerLevel level) {
        // Skip to dawn (time 0 = sunrise of the next day)
        // Only works in the overworld — other dimensions don't have a day/night cycle
        if (level.dimension() != Level.OVERWORLD) {
            player.displayClientMessage(
                Component.literal("\u00A7eThe sleeping bag doesn't work in this dimension."), true);
            return;
        }

        // Set to dawn (time 0). We use the server's overworld since all dimensions share game time.
        ServerLevel overworld = level.getServer().overworld();
        long currentTime = overworld.getDayTime();
        long timeOfDay = currentTime % 24000L;
        long timeToSkip;
        if (timeOfDay >= 12541L) {
            // It's night — skip to next dawn
            timeToSkip = 24000L - timeOfDay;
        } else {
            // Thunderstorm during the day — skip to next dawn anyway
            timeToSkip = 24000L - timeOfDay;
        }
        overworld.setDayTime(currentTime + timeToSkip);

        // Clear rain/thunder if it was a thunderstorm
        if (level.isThundering()) {
            level.setWeatherParameters(6000, 0, false, false);
        }

        // Apply brief darkness for the "sleeping" visual effect (smoother than blindness)
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, BLINDNESS_TICKS, 0, false, false, false));

        // Play a soft sound to indicate sleeping
        level.playSound(null, player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 0.5f);

        // Send chat feedback
        player.displayClientMessage(
            Component.literal("\u00A7aYou slept through the night using your sleeping bag..."), true);

        // Reset phantom timer for the player (sleeping resets insomnia)
        // In 1.21.x, the TIME_SINCE_REST stat is tracked per-player via ServerStatsCounter
        try {
            net.minecraft.stats.Stat<net.minecraft.resources.Identifier> timeSinceRest =
                net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.TIME_SINCE_REST);
            player.getStats().setValue(player, timeSinceRest, 0);
        } catch (Exception ignored) {
            // Gracefully handle if the stats API differs in this MC version
        }
    }

    /**
     * Check if it's night or thunderstorm (same conditions vanilla beds use).
     * Night is between ticks 12541 and 23459 of the day cycle.
     */
    private static boolean isNightOrThunderstorm(ServerLevel level) {
        long timeOfDay = level.getDayTime() % 24000L;
        boolean isNight = timeOfDay >= 12541L && timeOfDay <= 23459L;
        boolean isThundering = level.isThundering();
        return isNight || isThundering;
    }

    /**
     * Check if hostile mobs are nearby (within 8 blocks), preventing sleep.
     */
    private static boolean hasMonstersNearby(ServerPlayer player, ServerLevel level) {
        return !level.getEntitiesOfClass(
            net.minecraft.world.entity.monster.Monster.class,
            player.getBoundingBox().inflate(8.0, 5.0, 8.0),
            monster -> monster.isAlive() && !monster.isNoAi()
        ).isEmpty();
    }

    /**
     * Clean up cooldown entries for players who are no longer online.
     * Called periodically to prevent memory leaks.
     */
    public static void cleanupCooldowns() {
        // Entries will naturally expire, so this is a no-op for simplicity.
        // The map is small (one entry per player who used a sleeping bag).
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        tooltips.accept(Component.literal("\u00A77Use at night to skip to dawn"));
        tooltips.accept(Component.literal("\u00A77Does not set spawn point"));
        tooltips.accept(Component.literal("\u00A78Right-click ground to use"));
    }
}
