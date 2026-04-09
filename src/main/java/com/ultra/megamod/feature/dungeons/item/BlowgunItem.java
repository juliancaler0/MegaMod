package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.dungeons.entity.DartEntity;
import com.ultra.megamod.feature.dungeons.entity.DungeonEntityRegistry;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Blowgun with draw-and-release BOW animation.
 * Hold right-click to draw, release to fire a dart.
 * Velocity scales with draw time (min 5 ticks to fire, full power at 15 ticks).
 */
public class BlowgunItem extends Item {
    private static final float BASE_DAMAGE = 3.0f;
    private static final int MIN_DRAW_TICKS = 5;
    private static final float MAX_DRAW_TICKS = 15.0f;

    public BlowgunItem(Item.Properties props) {
        super(props.stacksTo(1).durability(200).rarity(Rarity.UNCOMMON));
    }

    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, BASE_DAMAGE, level.random);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack dartStack = findDartAmmo(player);
        if (dartStack.isEmpty() && !player.getAbilities().instabuild) {
            return InteractionResult.FAIL;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) return false;
        if (level.isClientSide()) return false;

        int useDuration = getUseDuration(stack, entity);
        int drawTime = useDuration - timeLeft;
        if (drawTime < MIN_DRAW_TICKS) return false;

        ItemStack dartStack = findDartAmmo(player);
        if (dartStack.isEmpty() && !player.getAbilities().instabuild) return false;

        ServerLevel serverLevel = (ServerLevel) level;
        float drawFraction = Math.min(1.0f, drawTime / MAX_DRAW_TICKS);
        float velocity = 0.5f + drawFraction * 1.5f;

        DartEntity dart = new DartEntity(DungeonEntityRegistry.DART.get(), level);
        dart.setOwner(player);
        dart.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        Vec3 look = player.getViewVector(1.0f);
        dart.shoot(look.x, look.y, look.z, velocity, 1.0f);
        level.addFreshEntity(dart);

        if (!player.getAbilities().instabuild && !dartStack.isEmpty()) {
            dartStack.shrink(1);
        }

        stack.hurtAndBreak(1, serverLevel, player, item -> {});
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                0.5f, 1.5f + drawFraction * 0.5f);
        return true;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    private ItemStack findDartAmmo(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof DartItem) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Draw and release to fire poison darts").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.literal("Requires Dart ammo").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }
}
