package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Sacred Scroll of Regeneration.
 * Applies Regeneration II to player AND all citizens within 16 blocks for 30 seconds.
 * NOT craftable (found in loot chests only).
 * No registration needed. Consumed on use.
 * Stacks to 16.
 */
public class ItemScrollBuff extends Item {

    private static final int BUFF_DURATION_TICKS = 30 * 20; // 30 seconds
    private static final int USE_DURATION = 32;
    private static final double CITIZEN_RADIUS = 16.0;

    public ItemScrollBuff(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide()) {
            return stack;
        }

        if (entity instanceof ServerPlayer player) {
            ServerLevel serverLevel = (ServerLevel) level;

            // Apply Regeneration II to the player
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION_TICKS, 1, false, true, true));

            // Find all citizens within 16 blocks and apply Regeneration II
            AABB searchBox = player.getBoundingBox().inflate(CITIZEN_RADIUS);
            List<MCEntityCitizen> citizens = serverLevel.getEntitiesOfClass(
                MCEntityCitizen.class, searchBox);

            int buffedCount = 0;
            for (MCEntityCitizen citizen : citizens) {
                citizen.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BUFF_DURATION_TICKS, 1, false, true, true));
                buffedCount++;
            }

            // Visual/audio feedback
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1, player.getZ(),
                24, 0.5, 0.5, 0.5, 0.0);
            // Particles on each citizen
            for (MCEntityCitizen citizen : citizens) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    citizen.getX(), citizen.getY() + 1, citizen.getZ(),
                    12, 0.3, 0.3, 0.3, 0.0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                SoundSource.PLAYERS, 1.0f, 1.2f);

            // Consume one scroll
            stack.shrink(1);

            player.displayClientMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76Regeneration II applied to you and " + buffedCount + " citizen(s) for 30 seconds!"), false);
        }

        return stack;
    }
}
