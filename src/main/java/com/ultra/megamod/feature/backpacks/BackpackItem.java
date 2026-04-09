package com.ultra.megamod.feature.backpacks;

import com.ultra.megamod.feature.backpacks.menu.BackpackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class BackpackItem extends Item {
    private final BackpackVariant variant;

    public BackpackItem(BackpackVariant variant, Properties props) {
        super(props);
        this.variant = variant;
    }

    public BackpackVariant getVariant() { return variant; }

    public BackpackTier getTier(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("BackpackTier")) {
                return BackpackTier.fromString(tag.getStringOr("BackpackTier", variant.getDefaultTier().name()));
            }
        }
        return variant.getDefaultTier();
    }

    public void setTier(ItemStack stack, BackpackTier tier) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putString("BackpackTier", tier.name());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Initialize backpack data if not already set.
     */
    public static void ensureInitialized(ItemStack stack) {
        if (!(stack.getItem() instanceof BackpackItem bpItem)) return;
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("BackpackTier")) {
            tag.putString("BackpackTier", bpItem.variant.getDefaultTier().name());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Sneak + right-click on a block to place the backpack as a block.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (level.isClientSide()) return InteractionResult.SUCCESS;

            // Calculate placement position
            BlockPos clickedPos = context.getClickedPos();
            Direction face = context.getClickedFace();
            BlockPos placePos = clickedPos.relative(face);

            // Check if we can place the block there
            BlockState existingState = level.getBlockState(placePos);
            if (!existingState.canBeReplaced()) {
                return InteractionResult.FAIL;
            }

            // Place the backpack block
            BackpackBlock backpackBlock = BackpackRegistry.BACKPACK_BLOCK.get();
            Direction playerFacing = player.getDirection().getOpposite();
            BlockState blockState = backpackBlock.defaultBlockState()
                    .setValue(BackpackBlock.FACING, playerFacing);

            level.setBlock(placePos, blockState, 3);

            // Transfer item data to block entity
            net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(placePos);
            if (be instanceof BackpackBlockEntity backpackBE) {
                ItemStack stack = context.getItemInHand();
                ensureInitialized(stack);
                backpackBE.fromItemStack(stack);
                backpackBE.setOwnerName(player.getGameProfile().name());

                // Consume the item
                stack.shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS; // fall through to use() for opening
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Don't open menu if sneaking (placement handled by useOn)
        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        ensureInitialized(stack);

        if (player instanceof ServerPlayer serverPlayer) {
            BackpackTier tier = getTier(stack);
            SimpleContainer container = BackpackMenu.loadFromItemStack(stack, tier);
            SimpleContainer toolContainer = BackpackMenu.loadToolsFromItemStack(stack, tier);
            com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager upgradeMgr = new com.ultra.megamod.feature.backpacks.upgrade.UpgradeManager(tier);
            upgradeMgr.initializeFromStack(stack);
            // Find which slot the backpack is in
            int slotIndex = hand == InteractionHand.MAIN_HAND
                ? findSelectedSlot(player)
                : 40; // offhand slot

            serverPlayer.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new BackpackMenu(id, inv, container, toolContainer, upgradeMgr, tier.ordinal(), slotIndex),
                Component.literal(variant.getDisplayName() + " Backpack")
            ), buf -> buf.writeInt(tier.ordinal()));
        }
        return InteractionResult.SUCCESS;
    }

    private static int findSelectedSlot(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i) == mainHand) {
                return i;
            }
        }
        return 0;
    }

    public void appendHoverText(ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltips, TooltipFlag flag) {
        BackpackTier tier = getTier(stack);
        tooltips.accept(Component.literal("\u00A77Tier: \u00A7e" + tier.getDisplayName()));
        tooltips.accept(Component.literal("\u00A77Storage: \u00A7a" + tier.getStorageSlots() + " slots"));
        tooltips.accept(Component.literal("\u00A77Upgrades: \u00A7b" + tier.getUpgradeSlots() + " slots"));
        tooltips.accept(Component.literal("\u00A77Tools: \u00A7d" + tier.getToolSlots() + " slots"));

        // Contents preview from stored items
        SimpleContainer container = BackpackMenu.loadFromItemStack(stack, tier);
        int totalSlots = tier.getStorageSlots();
        int usedSlots = 0;
        java.util.LinkedHashMap<String, Integer> uniqueItems = new java.util.LinkedHashMap<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slotItem = container.getItem(i);
            if (!slotItem.isEmpty()) {
                usedSlots++;
                String name = slotItem.getHoverName().getString();
                uniqueItems.merge(name, slotItem.getCount(), Integer::sum);
            }
        }
        if (usedSlots > 0) {
            tooltips.accept(Component.literal("\u00A77Contents: \u00A7f" + usedSlots + "/" + totalSlots + " slots used"));
            int shown = 0;
            for (java.util.Map.Entry<String, Integer> entry : uniqueItems.entrySet()) {
                if (shown >= 5) break;
                tooltips.accept(Component.literal("  \u00A78- \u00A7f" + entry.getValue() + "x " + entry.getKey()));
                shown++;
            }
            int remaining = uniqueItems.size() - shown;
            if (remaining > 0) {
                tooltips.accept(Component.literal("  \u00A78... and " + remaining + " more"));
            }
        } else {
            tooltips.accept(Component.literal("\u00A78Empty"));
        }

        tooltips.accept(Component.literal("\u00A78Sneak + Right-click to place"));
    }
}
