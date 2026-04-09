package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Magic Potion item that applies a random positive effect for 30 seconds.
 * Uses drinking animation before applying the effect.
 */
public class ItemMagicPotion extends Item {

    private static final int EFFECT_DURATION_TICKS = 30 * 20; // 30 seconds
    private static final int DRINK_DURATION = 32; // ticks to drink

    @SuppressWarnings("unchecked")
    private static final List<Holder<MobEffect>> POSITIVE_EFFECTS = List.of(
            (Holder<MobEffect>) MobEffects.SPEED,
            (Holder<MobEffect>) MobEffects.STRENGTH,
            (Holder<MobEffect>) MobEffects.REGENERATION,
            (Holder<MobEffect>) MobEffects.RESISTANCE,
            (Holder<MobEffect>) MobEffects.JUMP_BOOST
    );

    public ItemMagicPotion(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_DURATION;
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

            // Pick a random positive effect
            Holder<MobEffect> effect = POSITIVE_EFFECTS.get(
                    level.getRandom().nextInt(POSITIVE_EFFECTS.size()));
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, 0, false, true, true));

            // Visual/audio feedback
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    player.getX(), player.getY() + 1, player.getZ(),
                    16, 0.3, 0.5, 0.3, 0.0);
            level.playSound(null, player.blockPosition(), SoundEvents.BREWING_STAND_BREW,
                    SoundSource.PLAYERS, 1.0f, 1.0f);

            // Get effect name for message
            String effectName = effect.value().getDescriptionId();
            // Simple name extraction from translation key
            String simpleName = effectName.contains(".")
                    ? effectName.substring(effectName.lastIndexOf('.') + 1)
                    : effectName;
            simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);

            player.displayClientMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A7dMagic Potion granted \u00A7f" + simpleName
                + "\u00A7d for 30 seconds!"), false);

            // Consume one potion
            stack.shrink(1);
        }

        return stack;
    }
}
