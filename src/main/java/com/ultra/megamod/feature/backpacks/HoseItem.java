package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager;
import com.ultra.megamod.feature.backpacks.upgrade.tanks.TanksUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

/**
 * Utility item for interacting with the backpack's Tanks upgrade.
 * Modes cycle with shift-right-click in air: FILL_LEFT, FILL_RIGHT, DRAIN_LEFT, DRAIN_RIGHT.
 * Right-click on a fluid source block to fill the tank.
 * Right-click on a solid block to drain from the tank and place fluid.
 */
public class HoseItem extends Item {

    public enum HoseMode {
        FILL_LEFT("Fill Left Tank"),
        FILL_RIGHT("Fill Right Tank"),
        DRAIN_LEFT("Drain Left Tank"),
        DRAIN_RIGHT("Drain Right Tank");

        private final String displayName;

        HoseMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }

        public HoseMode next() {
            HoseMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    public HoseItem(Properties props) {
        super(props.stacksTo(1));
    }

    public static HoseMode getMode(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            String modeName = tag.getStringOr("HoseMode", "FILL_LEFT");
            try {
                return HoseMode.valueOf(modeName);
            } catch (IllegalArgumentException e) {
                // fall through
            }
        }
        return HoseMode.FILL_LEFT;
    }

    public static void setMode(ItemStack stack, HoseMode mode) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString("HoseMode", mode.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Shift+right-click in air: cycle mode
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                HoseMode current = getMode(stack);
                HoseMode next = current.next();
                setMode(stack, next);
                player.displayClientMessage(
                    Component.literal("\u00A7eHose mode: \u00A7f" + next.getDisplayName()), true);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        // Shift+right-click cycles mode (handled by use())
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;

        ItemStack hoseStack = context.getItemInHand();
        HoseMode mode = getMode(hoseStack);

        // Get the worn backpack
        if (!BackpackWearableManager.isWearing(player.getUUID())) {
            player.displayClientMessage(
                Component.literal("\u00A7cYou must have a backpack equipped!"), true);
            return InteractionResult.FAIL;
        }

        ItemStack backpackStack = BackpackWearableManager.getEquipped(player.getUUID());
        if (backpackStack.isEmpty() || !(backpackStack.getItem() instanceof BackpackItem bpItem)) {
            return InteractionResult.FAIL;
        }

        BackpackTier tier = bpItem.getTier(backpackStack);
        UpgradeManager upgradeMgr = new UpgradeManager(tier);
        upgradeMgr.initializeFromStack(backpackStack);

        TanksUpgrade tanksUpgrade = upgradeMgr.getUpgrade(TanksUpgrade.class);
        if (tanksUpgrade == null) {
            player.displayClientMessage(
                Component.literal("\u00A7cBackpack has no Tanks upgrade installed!"), true);
            return InteractionResult.FAIL;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        if (mode == HoseMode.FILL_LEFT || mode == HoseMode.FILL_RIGHT) {
            return handleFill(serverPlayer, level, clickedPos, clickedState, backpackStack,
                    upgradeMgr, tanksUpgrade, mode);
        } else {
            return handleDrain(serverPlayer, level, context, backpackStack,
                    upgradeMgr, tanksUpgrade, mode);
        }
    }

    private InteractionResult handleFill(ServerPlayer player, Level level, BlockPos pos,
                                          BlockState state, ItemStack backpackStack,
                                          UpgradeManager upgradeMgr, TanksUpgrade tanksUpgrade,
                                          HoseMode mode) {
        // Determine if the clicked block is a fluid source
        String fluidId = null;
        if (state.getFluidState().isSource()) {
            if (state.getFluidState().getType() == Fluids.WATER) {
                fluidId = "minecraft:water";
            } else if (state.getFluidState().getType() == Fluids.LAVA) {
                fluidId = "minecraft:lava";
            }
        }

        if (fluidId == null) {
            player.displayClientMessage(
                Component.literal("\u00A7cNot a fluid source block!"), true);
            return InteractionResult.FAIL;
        }

        // Try to fill the tank
        int inserted;
        if (mode == HoseMode.FILL_LEFT) {
            inserted = tanksUpgrade.fillLeft(fluidId, 1000);
        } else {
            inserted = tanksUpgrade.fillRight(fluidId, 1000);
        }

        if (inserted <= 0) {
            player.displayClientMessage(
                Component.literal("\u00A7cTank is full or contains a different fluid!"), true);
            return InteractionResult.FAIL;
        }

        // Remove the source block
        if (state.getBlock() instanceof BucketPickup bucketPickup) {
            bucketPickup.pickupBlock(player, level, pos, state);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        // Save back to backpack
        upgradeMgr.saveToStack(backpackStack);

        // Play sound
        if (fluidId.equals("minecraft:water")) {
            level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL_LAVA, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        String tankLabel = mode == HoseMode.FILL_LEFT ? "left" : "right";
        player.displayClientMessage(
            Component.literal("\u00A7aFilled " + tankLabel + " tank with 1000mB"), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleDrain(ServerPlayer player, Level level, UseOnContext context,
                                           ItemStack backpackStack, UpgradeManager upgradeMgr,
                                           TanksUpgrade tanksUpgrade, HoseMode mode) {
        // Drain from the tank and place fluid in the world
        String fluidId;
        int drained;
        if (mode == HoseMode.DRAIN_LEFT) {
            fluidId = tanksUpgrade.getLeftFluidId();
            drained = tanksUpgrade.drainLeft(1000);
        } else {
            fluidId = tanksUpgrade.getRightFluidId();
            drained = tanksUpgrade.drainRight(1000);
        }

        if (drained < 1000 || fluidId.isEmpty()) {
            // Refund if we drained less than a full bucket
            if (drained > 0) {
                if (mode == HoseMode.DRAIN_LEFT) {
                    tanksUpgrade.fillLeft(fluidId, drained);
                } else {
                    tanksUpgrade.fillRight(fluidId, drained);
                }
            }
            player.displayClientMessage(
                Component.literal("\u00A7cNot enough fluid in tank (need 1000mB)!"), true);
            return InteractionResult.FAIL;
        }

        // Only support water and lava placement
        if (!fluidId.equals("minecraft:water") && !fluidId.equals("minecraft:lava")) {
            // Refund
            if (mode == HoseMode.DRAIN_LEFT) {
                tanksUpgrade.fillLeft(fluidId, drained);
            } else {
                tanksUpgrade.fillRight(fluidId, drained);
            }
            player.displayClientMessage(
                Component.literal("\u00A7cCan only place water or lava!"), true);
            return InteractionResult.FAIL;
        }

        // Find the placement position (adjacent to the clicked face)
        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        BlockState existingState = level.getBlockState(placePos);

        if (!existingState.canBeReplaced()) {
            // Refund
            if (mode == HoseMode.DRAIN_LEFT) {
                tanksUpgrade.fillLeft(fluidId, drained);
            } else {
                tanksUpgrade.fillRight(fluidId, drained);
            }
            player.displayClientMessage(
                Component.literal("\u00A7cCannot place fluid there!"), true);
            return InteractionResult.FAIL;
        }

        // Place the fluid
        if (fluidId.equals("minecraft:water")) {
            level.setBlock(placePos, Blocks.WATER.defaultBlockState(), 3);
            level.playSound(null, placePos, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
        } else {
            level.setBlock(placePos, Blocks.LAVA.defaultBlockState(), 3);
            level.playSound(null, placePos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.PLAYERS, 1.0f, 1.0f);
        }

        // Save back to backpack
        upgradeMgr.saveToStack(backpackStack);

        String tankLabel = mode == HoseMode.DRAIN_LEFT ? "left" : "right";
        player.displayClientMessage(
            Component.literal("\u00A7eDrained 1000mB from " + tankLabel + " tank"), true);
        return InteractionResult.SUCCESS;
    }

    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        HoseMode mode = getMode(stack);
        tooltips.accept(Component.literal("\u00A77Mode: \u00A7f" + mode.getDisplayName()));
        tooltips.accept(Component.literal("\u00A78Right-click fluid source to fill tank"));
        tooltips.accept(Component.literal("\u00A78Right-click block to drain & place fluid"));
        tooltips.accept(Component.literal("\u00A78Shift+Right-click in air to cycle mode"));
    }
}
