package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ScepterOfChaosItem extends Item {

    private static final float BASE_DAMAGE = 4.0f;

    public ScepterOfChaosItem(Item.Properties props) {
        super(props.stacksTo(1).durability(200).rarity(Rarity.EPIC));
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, BASE_DAMAGE, level.random);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        AABB area = player.getBoundingBox().inflate(16.0);
        Vec3 lookVec = player.getViewVector(1.0f).normalize();

        LivingEntity bestTarget = null;
        double bestDot = -1;

        List<LivingEntity> entities = serverLevel.getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive() && !(e instanceof Player));

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().subtract(player.position()).normalize();
            double dot = lookVec.dot(toEntity);
            if (dot > 0.5 && dot > bestDot) {
                bestDot = dot;
                bestTarget = entity;
            }
        }

        if (bestTarget != null) {
            bestTarget.hurt(player.damageSources().magic(), 10.0f);

            Vec3 start = player.position().add(0, 1.5, 0);
            Vec3 end = bestTarget.position().add(0, 1.0, 0);
            Vec3 dir = end.subtract(start).normalize();
            double dist = start.distanceTo(end);
            for (int i = 0; i < (int) (dist * 3); i++) {
                double t = (double) i / (dist * 3);
                serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    start.x + dir.x * dist * t,
                    start.y + dir.y * dist * t,
                    start.z + dir.z * dist * t,
                    1, 0.05, 0.05, 0.05, 0.0);
            }

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 0.8f);
            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            player.getCooldowns().addCooldown(stack, 20);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Scepter of Chaos").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right-click to fire a magic bolt").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Targets nearest entity in line of sight").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("10 damage | 1s cooldown | 200 uses").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
