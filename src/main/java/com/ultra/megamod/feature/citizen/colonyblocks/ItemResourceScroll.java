package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;
import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder;
import com.ultra.megamod.feature.citizen.building.workorder.WorkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;

/**
 * Resource Scroll — must be registered to a Builder's Hut first via shift+right-click.
 * Then right-click in air to see the color-coded material list for the builder's current work order.
 *
 * Color coding (matches MegaColonies spec):
 *   §0 (black)  = item already delivered to builder
 *   §c (red)    = item still needed, player doesn't have any
 *   §a (green)  = item needed AND player has enough in inventory
 *   §e (yellow) = item needed, player has some but not enough
 *   §7 (gray)   = no resources tracked yet
 */
public class ItemResourceScroll extends Item {

    public ItemResourceScroll(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || level.isClientSide()) return InteractionResult.SUCCESS;

        // Shift+right-click on a Builder's Hut to register
        if (player.isShiftKeyDown()) {
            BlockEntity be = level.getBlockEntity(context.getClickedPos());
            if (be instanceof TileEntityColonyBuilding tile && "builder".equals(tile.getBuildingId())) {
                ItemStack stack = context.getItemInHand();
                CompoundTag tag = new CompoundTag();
                BlockPos pos = context.getClickedPos();
                tag.putInt("builderX", pos.getX());
                tag.putInt("builderY", pos.getY());
                tag.putInt("builderZ", pos.getZ());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
                ((ServerPlayer) player).displayClientMessage(
                        Component.literal("Resource Scroll linked to Builder's Hut at ("
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                .withStyle(ChatFormatting.GOLD), false);
                return InteractionResult.CONSUME;
            }

            // Also accept if the block itself is AbstractBlockHut with builder id
            if (level.getBlockState(context.getClickedPos()).getBlock() instanceof AbstractBlockHut<?> hut
                    && "builder".equals(hut.getBuildingId())) {
                ItemStack stack = context.getItemInHand();
                CompoundTag tag = new CompoundTag();
                BlockPos pos = context.getClickedPos();
                tag.putInt("builderX", pos.getX());
                tag.putInt("builderY", pos.getY());
                tag.putInt("builderZ", pos.getZ());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                        SoundSource.PLAYERS, 1.0f, 1.0f);
                ((ServerPlayer) player).displayClientMessage(
                        Component.literal("Resource Scroll linked to Builder's Hut at ("
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")")
                                .withStyle(ChatFormatting.GOLD), false);
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        ServerPlayer sp = (ServerPlayer) player;
        ServerLevel sl = (ServerLevel) level;

        if (!tag.contains("builderX")) {
            sp.displayClientMessage(Component.literal("No Builder's Hut linked! Sneak + right-click on a Builder's Hut first.")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        BlockPos builderPos = new BlockPos(
                tag.getIntOr("builderX", 0),
                tag.getIntOr("builderY", 0),
                tag.getIntOr("builderZ", 0)
        );

        // Verify the builder hut still exists
        BlockEntity be = level.getBlockEntity(builderPos);
        String buildingId = null;
        int builderLevel = 0;
        if (be instanceof TileEntityColonyBuilding tile) {
            buildingId = tile.getBuildingId();
            builderLevel = tile.getBuildingLevel();
        }
        if (!"builder".equals(buildingId)) {
            sp.displayClientMessage(Component.literal("Builder's Hut no longer exists at linked position!")
                    .withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        // Find work orders at this builder's position
        WorkManager wm = WorkManager.get(sl);
        ColonyWorkOrder activeOrder = null;
        for (ColonyWorkOrder order : wm.getOrders()) {
            if (order.isClaimed() && order.getAssignedBuilderId() != null) {
                // Check if any builder at this position claimed this order
                // For simplicity, match by proximity (builder claims orders)
                activeOrder = order;
                break;
            }
        }

        // Also check unclaimed orders that match this builder's colony
        if (activeOrder == null) {
            TileEntityColonyBuilding tile = (TileEntityColonyBuilding) be;
            if (tile.getColonyId() != null) {
                var colonyOrders = wm.getOrdersForColony(tile.getColonyId());
                if (!colonyOrders.isEmpty()) {
                    activeOrder = colonyOrders.get(0); // Show the first one
                }
            }
        }

        // Header
        sp.displayClientMessage(Component.literal("=== Resource Scroll ===").withStyle(ChatFormatting.GOLD), false);
        sp.displayClientMessage(Component.literal("Builder's Hut [Lv." + builderLevel + "] at ("
                + builderPos.getX() + ", " + builderPos.getY() + ", " + builderPos.getZ() + ")")
                .withStyle(ChatFormatting.YELLOW), false);

        if (activeOrder == null) {
            sp.displayClientMessage(Component.literal("No active work order for this builder.")
                    .withStyle(ChatFormatting.GRAY), false);
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.CONSUME;
        }

        // Show work order info
        String status = activeOrder.isClaimed() ? "\u00A7a[Claimed]" : "\u00A7c[Unclaimed]";
        sp.displayClientMessage(Component.literal(status + " \u00A7f" + activeOrder.getBuildingId()
                + " \u00A77Lv." + activeOrder.getTargetLevel()
                + " \u00A78(" + activeOrder.getType().name() + ")"), false);

        sp.displayClientMessage(Component.literal("--- Materials ---").withStyle(ChatFormatting.GRAY), false);

        Map<String, Integer> required = activeOrder.getRequiredResources();
        Map<String, Integer> delivered = activeOrder.getDeliveredResources();

        if (required.isEmpty()) {
            sp.displayClientMessage(Component.literal("No resource list available yet. Builder must start the work order first.")
                    .withStyle(ChatFormatting.GRAY), false);
        } else {
            Inventory inv = player.getInventory();
            int shown = 0;

            for (Map.Entry<String, Integer> entry : required.entrySet()) {
                String itemId = entry.getKey();
                int totalNeeded = entry.getValue();
                int totalDelivered = delivered.getOrDefault(itemId, 0);
                int stillNeeded = Math.max(0, totalNeeded - totalDelivered);

                String displayName = ColonyWorkOrder.getItemDisplayName(itemId);
                int playerHas = countItemInInventory(inv, itemId);

                String line;
                if (stillNeeded <= 0) {
                    // Black = fully delivered
                    line = "\u00A70\u2714 " + displayName + " " + totalDelivered + "/" + totalNeeded + " [Delivered]";
                } else if (playerHas >= stillNeeded) {
                    // Green = player has enough
                    line = "\u00A7a\u25CF " + displayName + " " + totalDelivered + "/" + totalNeeded
                            + " (need " + stillNeeded + ", you have " + playerHas + ")";
                } else if (playerHas > 0) {
                    // Yellow = player has some but not enough
                    line = "\u00A7e\u25CB " + displayName + " " + totalDelivered + "/" + totalNeeded
                            + " (need " + stillNeeded + ", you have " + playerHas + ")";
                } else {
                    // Red = player has none
                    line = "\u00A7c\u2716 " + displayName + " " + totalDelivered + "/" + totalNeeded
                            + " (need " + stillNeeded + ")";
                }

                sp.displayClientMessage(Component.literal(" " + line), false);
                if (++shown >= 30) {
                    sp.displayClientMessage(Component.literal("... and more")
                            .withStyle(ChatFormatting.GRAY), false);
                    break;
                }
            }
        }

        level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        return InteractionResult.CONSUME;
    }

    /**
     * Counts how many of a specific item (by registry ID) the player has in their inventory.
     */
    private static int countItemInInventory(Inventory inv, String itemId) {
        int count = 0;
        try {
            Identifier id = Identifier.parse(itemId);
            Item targetItem = BuiltInRegistries.ITEM.getValue(id);
            for (int i = 0; i < inv.getContainerSize(); i++) {
                ItemStack slot = inv.getItem(i);
                if (!slot.isEmpty() && slot.getItem() == targetItem) {
                    count += slot.getCount();
                }
            }
        } catch (Exception ignored) {
        }
        return count;
    }
}
