/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Item$TooltipContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.component.TooltipDisplay
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OssukageSwordItem
extends Item {
    private static final float BASE_DAMAGE = 9.0f;

    public OssukageSwordItem(Item.Properties props) {
        super(props.fireResistant().rarity(Rarity.EPIC).stacksTo(1));
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, BASE_DAMAGE, level.random);
        }
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide()) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (player.isShiftKeyDown()) {
                Vec3 look = player.getViewVector(1.0f).normalize();
                SmallFireball fireball = new SmallFireball((Level)serverLevel, (LivingEntity)player, new Vec3(look.x, look.y, look.z));
                fireball.setPos(player.getX() + look.x * 1.5, player.getEyeY() - 0.1, player.getZ() + look.z * 1.5);
                serverLevel.addFreshEntity((Entity)fireball);
                serverLevel.playSound(null, player.blockPosition(), (SoundEvent)SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
            } else {
                Vec3 look = player.getViewVector(1.0f).normalize();
                player.push(look.x * 2.0, 0.2, look.z * 2.0);
                player.hurtMarked = true;
                AABB dashArea = player.getBoundingBox().inflate(2.0).move(look.scale(2.0));
                List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, dashArea, e -> e != player && e.isAlive());
                for (LivingEntity target : targets) {
                    target.hurt(serverLevel.damageSources().playerAttack(player), 12.0f);
                    Vec3 kb = target.position().subtract(player.position()).normalize().scale(1.5);
                    target.push(kb.x, 0.3, kb.z);
                }
                for (int i = 0; i < 10; ++i) {
                    double px = player.getX() + look.x * (double)i * 0.3;
                    double pz = player.getZ() + look.z * (double)i * 0.3;
                    serverLevel.sendParticles((ParticleOptions)ParticleTypes.CLOUD, px, player.getY() + 0.5, pz, 1, 0.1, 0.1, 0.1, 0.0);
                }
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.5f, 0.8f);
            }
            player.getCooldowns().addCooldown(stack, 20);
        }
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept((Component)Component.empty());
        tooltip.accept((Component)Component.literal((String)"Ossukage's Blade").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD}));
        tooltip.accept((Component)Component.empty());
        tooltip.accept((Component)Component.literal((String)"Skills:").withStyle(ChatFormatting.GOLD));
        tooltip.accept((Component)Component.literal((String)"  Dash Attack (1.0s)").withStyle(ChatFormatting.YELLOW));
        tooltip.accept((Component)Component.literal((String)"    Lunge forward and damage nearby enemies").withStyle(ChatFormatting.GRAY));
        tooltip.accept((Component)Component.literal((String)"  Kunai Throw [Shift] (1.0s)").withStyle(ChatFormatting.YELLOW));
        tooltip.accept((Component)Component.literal((String)"    Hurl a flaming projectile").withStyle(ChatFormatting.GRAY));
        tooltip.accept((Component)Component.empty());
        tooltip.accept((Component)Component.literal((String)"Dungeon Boss Drop").withStyle(ChatFormatting.DARK_PURPLE));
    }

    public boolean isFoil(ItemStack stack) {
        return true;
    }
}

