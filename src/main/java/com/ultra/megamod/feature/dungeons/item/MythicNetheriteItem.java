package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.ArmorStatRoller;
import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Mythic Netherite gear — superior to vanilla Netherite.
 * Dungeon-exclusive: only drops from Mythic+ tier dungeons.
 * Armor uses ArmorStatRoller; Sword/Axe use WeaponStatRoller.
 */
public class MythicNetheriteItem extends Item {

    public enum Piece {
        SWORD("Sword", null, 11.0, 0.0),
        AXE("Axe", null, 13.0, 0.0),
        HELMET("Helmet", EquipmentSlot.HEAD, 4.5, 3.5),
        CHESTPLATE("Chestplate", EquipmentSlot.CHEST, 10.0, 4.0),
        LEGGINGS("Leggings", EquipmentSlot.LEGS, 8.0, 3.5),
        BOOTS("Boots", EquipmentSlot.FEET, 4.5, 3.5);

        public final String displayName;
        @Nullable public final EquipmentSlot slot;
        public final double primaryStat; // armor for armor pieces, attack damage for weapons
        public final double toughness;

        Piece(String displayName, @Nullable EquipmentSlot slot, double primaryStat, double toughness) {
            this.displayName = displayName;
            this.slot = slot;
            this.primaryStat = primaryStat;
            this.toughness = toughness;
        }

        public boolean isArmor() { return slot != null; }
        public boolean isWeapon() { return this == SWORD || this == AXE; }
    }

    private final Piece piece;

    public MythicNetheriteItem(Item.Properties props, Piece piece) {
        super(buildProperties(props, piece));
        this.piece = piece;
    }

    private static Item.Properties buildProperties(Item.Properties props, Piece piece) {
        props = props.stacksTo(1).rarity(Rarity.EPIC).fireResistant();
        if (piece.isArmor()) {
            props = props.durability(getArmorDurability(piece)).equippable(piece.slot);
        } else {
            props = props.durability(2500);
        }
        return props;
    }

    private static int getArmorDurability(Piece piece) {
        return switch (piece) {
            case HELMET -> 550;
            case CHESTPLATE -> 800;
            case LEGGINGS -> 750;
            case BOOTS -> 650;
            default -> 500;
        };
    }

    public Piece getPiece() { return piece; }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot(ItemStack stack) {
        return piece.slot;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot equippedSlot) {
        if (piece.isArmor() && !ArmorStatRoller.isArmorInitialized(stack)) {
            ArmorStatRoller.rollAndApply(stack, piece.primaryStat, piece.toughness, piece.slot, level.random);
        }
        if (piece.isWeapon() && !WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, (float) piece.primaryStat, level.random);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (piece.isArmor()) {
            ArmorStatRoller.appendArmorTooltip(stack, tooltip);
            tooltip.accept(Component.empty());
        }
        if (piece.isWeapon()) {
            WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
            tooltip.accept(Component.empty());
        }
        tooltip.accept(Component.literal("Mythic Netherite " + piece.displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.accept(Component.literal("Superior to Netherite").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Mythic+ Dungeon Exclusive").withStyle(ChatFormatting.DARK_RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    // --- Axe behavior: stripping, scraping, waxing ---

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        if (piece == Piece.AXE) {
            return ability == ItemAbilities.AXE_STRIP
                || ability == ItemAbilities.AXE_SCRAPE
                || ability == ItemAbilities.AXE_WAX_OFF;
        }
        if (piece == Piece.SWORD) {
            return ability == ItemAbilities.SWORD_SWEEP;
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (piece != Piece.AXE) return super.useOn(context);

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();

        // Try stripping (logs -> stripped logs)
        Optional<BlockState> strippedState = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false));
        if (strippedState.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide()) {
                level.setBlock(pos, strippedState.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, strippedState.get()));
                if (player != null) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Try scraping (oxidized copper -> less oxidized)
        Optional<BlockState> scrapedState = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false));
        if (scrapedState.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide()) {
                level.setBlock(pos, scrapedState.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, scrapedState.get()));
                if (player != null) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Try wax off (waxed copper -> unwaxed)
        Optional<BlockState> unwaxedState = Optional.ofNullable(state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false));
        if (unwaxedState.isPresent()) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!level.isClientSide()) {
                level.setBlock(pos, unwaxedState.get(), 11);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, unwaxedState.get()));
                if (player != null) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (piece == Piece.AXE) {
            if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
                return 10.0f; // Slightly faster than netherite (9.0)
            }
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (piece == Piece.AXE && state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return true;
        }
        return super.isCorrectToolForDrops(stack, state);
    }
}
