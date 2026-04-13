package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class GeomancerArmorItem extends Item {

    private final String pieceName;
    private final EquipmentSlot slot;
    private final double baseArmor;
    private final double baseToughness;

    /**
     * Mirrors the RpgArmorItem pattern — Properties (including EQUIPPABLE) are
     * built in a supplier at registration time, not inside the constructor.
     */
    public GeomancerArmorItem(Item.Properties props, String pieceName) {
        super(props);
        this.pieceName = pieceName;
        this.slot = slotFor(pieceName);
        this.baseArmor = switch (this.slot) {
            case HEAD -> 3.0;
            case CHEST -> 7.0;
            case LEGS -> 5.0;
            case FEET -> 2.0;
            default -> 2.0;
        };
        this.baseToughness = switch (this.slot) {
            case HEAD -> 1.0;
            case CHEST -> 2.0;
            case LEGS -> 1.5;
            case FEET -> 1.0;
            default -> 1.0;
        };
    }

    private static EquipmentSlot slotFor(String pieceName) {
        return switch (pieceName) {
            case "Helm" -> EquipmentSlot.HEAD;
            case "Chestplate" -> EquipmentSlot.CHEST;
            case "Leggings" -> EquipmentSlot.LEGS;
            case "Boots" -> EquipmentSlot.FEET;
            default -> EquipmentSlot.HEAD;
        };
    }

    @Override
    public @javax.annotation.Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return this.slot;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot equippedSlot) {
        if (!ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, baseArmor, baseToughness, this.slot, level.random);
        }

        if (!(entity instanceof Player player)) return;
        if (player.getItemBySlot(this.slot) != stack) return;
        if (level.getGameTime() % 40 != 0) return;

        int pieces = countGeomancerPieces(player);
        if (pieces >= 4) {
            applyEarthSpikeRetaliation(player, (ServerLevel) level);
        }
    }

    private int countGeomancerPieces(Player player) {
        int count = 0;
        for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (player.getItemBySlot(s).getItem() instanceof GeomancerArmorItem) {
                count++;
            }
        }
        return count;
    }

    private void applyEarthSpikeRetaliation(Player player, ServerLevel level) {
        AABB area = player.getBoundingBox().inflate(4.0);
        List<Monster> nearby = level.getEntitiesOfClass(Monster.class, area, Monster::isAlive);
        if (nearby.isEmpty()) return;
        if (level.getRandom().nextFloat() >= 0.2f) return;

        Monster target = nearby.get(level.getRandom().nextInt(nearby.size()));
        BlockPos below = target.blockPosition().below();

        if (level.getBlockState(below).isAir() || level.getBlockState(below).canBeReplaced()) {
            level.setBlock(below, Blocks.COBBLESTONE.defaultBlockState(), 3);
        }

        BlockPos above = target.blockPosition();
        if (level.getBlockState(above).isAir()) {
            level.setBlock(above, Blocks.COBBLESTONE.defaultBlockState(), 3);
        }

        target.hurt(level.damageSources().magic(), 4.0f);
        level.playSound(null, target.blockPosition(), SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        ArmorStatRoller.appendArmorTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Geomancer " + pieceName).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("Per piece: +5% Knockback Resistance").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("4pc: Earth Spike Retaliation").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
