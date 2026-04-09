package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;
import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.entity.mc.MCEntityCitizen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Worker-Where-Are-You Scroll.
 * Shift+right-click on a hut block: highlights that building's assigned worker with
 * Glowing effect for 60 seconds + gives them Speed I for 60 seconds.
 * Does NOT need Town Hall registration.
 * Consumed on use.
 * Stacks to 16.
 */
public class ItemScrollHighlight extends Item {

    private static final int EFFECT_DURATION_TICKS = 60 * 20; // 60 seconds

    public ItemScrollHighlight(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockPos clickedPos = context.getClickedPos();
        BlockState state = level.getBlockState(clickedPos);

        // Check if the clicked block is a colony hut block
        if (!(state.getBlock() instanceof AbstractBlockHut<?>)) {
            ((ServerPlayer) player).displayClientMessage(Component.literal(
                "\u00A7c\u00A7l\u2716 \u00A76You must use this scroll on a colony hut block!"), false);
            return InteractionResult.FAIL;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer sp = (ServerPlayer) player;
        ItemStack stack = context.getItemInHand();

        // Get the building entity at this position
        BlockEntity be = level.getBlockEntity(clickedPos);
        if (!(be instanceof TileEntityColonyBuilding tile)) {
            sp.displayClientMessage(Component.literal(
                "\u00A7c\u00A7l\u2716 \u00A76This hut has no building data!"), false);
            return InteractionResult.FAIL;
        }

        // Find workers assigned to this building position
        int highlighted = 0;
        for (Entity entity : serverLevel.getAllEntities()) {
            if (entity instanceof MCEntityCitizen citizen) {
                // Check if citizen is near the building
                if (citizen.blockPosition().distSqr(clickedPos) <= 256) {
                    // Apply Glowing effect for 60 seconds
                    citizen.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, EFFECT_DURATION_TICKS, 0, false, true, true));
                    // Apply Speed I for 60 seconds
                    citizen.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, EFFECT_DURATION_TICKS, 0, false, true, true));
                    highlighted++;
                }
            }
        }

        if (highlighted == 0) {
            sp.displayClientMessage(Component.literal(
                "\u00A7c\u00A7l\u2716 \u00A76No workers found assigned to this building!"), false);
            return InteractionResult.FAIL;
        }

        // Sound effect
        level.playSound(null, clickedPos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
            1.0f, 1.5f);

        // Spawn particles at the hut
        serverLevel.sendParticles(ParticleTypes.END_ROD,
            clickedPos.getX() + 0.5, clickedPos.getY() + 1.5, clickedPos.getZ() + 0.5,
            20, 0.3, 1.0, 0.3, 0.02);

        // Consume one scroll
        stack.shrink(1);

        sp.displayClientMessage(Component.literal(
            "\u00A7a\u00A7l\u2714 \u00A76Highlighted " + highlighted + " worker(s) with Glowing + Speed I for 60 seconds!"), false);

        return InteractionResult.CONSUME;
    }
}
