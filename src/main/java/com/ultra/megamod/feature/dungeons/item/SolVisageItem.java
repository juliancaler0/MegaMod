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

public class SolVisageItem extends Item {

    private static final float BASE_DAMAGE = 5.0f;

    public SolVisageItem(Item.Properties props) {
        super(props.stacksTo(1).durability(100).rarity(Rarity.EPIC));
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
        Vec3 lookVec = player.getViewVector(1.0f).normalize();
        Vec3 start = player.position().add(0, 1.5, 0);

        for (int step = 1; step <= 20; step++) {
            Vec3 point = start.add(lookVec.scale(step));
            AABB hitbox = new AABB(
                point.x - 0.5, point.y - 0.5, point.z - 0.5,
                point.x + 0.5, point.y + 0.5, point.z + 0.5);

            List<LivingEntity> hit = serverLevel.getEntitiesOfClass(LivingEntity.class, hitbox,
                e -> e != player && e.isAlive());

            for (LivingEntity entity : hit) {
                entity.hurt(player.damageSources().magic(), 12.0f);
                entity.setRemainingFireTicks(60);
            }

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                point.x, point.y, point.z,
                3, 0.1, 0.1, 0.1, 0.01);
        }

        serverLevel.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.0f);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        player.getCooldowns().addCooldown(stack, 100);

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
        tooltip.accept(Component.literal("Sol Visage").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Right-click to fire a sun beam").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("20 block range | 12 damage + fire").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("5s cooldown | 100 uses").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
