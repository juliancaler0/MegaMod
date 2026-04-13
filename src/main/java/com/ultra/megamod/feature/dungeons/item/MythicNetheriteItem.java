package com.ultra.megamod.feature.dungeons.item;

import com.ultra.megamod.feature.relics.data.WeaponStatRoller;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Mythic Netherite weapons — superior to vanilla Netherite.
 * Dungeon-exclusive: only drops from Mythic+ tier dungeons.
 * The matching armor set was removed; Dungeon Netherite armor covers that role.
 */
public class MythicNetheriteItem extends Item {

    public enum Piece {
        SWORD("Sword", 11.0),
        AXE("Axe", 13.0);

        public final String displayName;
        public final double primaryStat;

        Piece(String displayName, double primaryStat) {
            this.displayName = displayName;
            this.primaryStat = primaryStat;
        }

        public boolean isArmor() { return false; }
        public boolean isWeapon() { return true; }
    }

    private final Piece piece;

    public MythicNetheriteItem(Item.Properties props, Piece piece) {
        super(props.stacksTo(1).rarity(Rarity.EPIC).fireResistant().durability(2500));
        this.piece = piece;
    }

    public Piece getPiece() { return piece; }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot equippedSlot) {
        if (!WeaponStatRoller.isWeaponInitialized(stack)) {
            WeaponStatRoller.rollAndApply(stack, (float) piece.primaryStat, level.random);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        WeaponStatRoller.appendWeaponTooltip(stack, tooltip);
        tooltip.accept(Component.empty());
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
