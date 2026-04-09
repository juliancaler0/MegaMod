package com.ultra.megamod.feature.citizen.colonyblocks;

import com.ultra.megamod.feature.citizen.building.AbstractBlockHut;
import com.ultra.megamod.feature.citizen.building.workorder.ColonyWorkOrder;
import com.ultra.megamod.feature.citizen.building.workorder.WorkManager;
import com.ultra.megamod.feature.citizen.request.IRequest;
import com.ultra.megamod.feature.citizen.request.RequestManager;
import com.ultra.megamod.feature.citizen.request.RequestState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Clipboard — must register to Town Hall first (shift+right-click on Town Hall),
 * then right-click in air to see all unfulfilled citizen requests + work orders.
 */
public class ItemClipboard extends Item {

    public ItemClipboard(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || level.isClientSide()) return InteractionResult.SUCCESS;

        // Shift+right-click on Town Hall to register
        if (player.isShiftKeyDown() && level.getBlockState(context.getClickedPos()).getBlock() instanceof AbstractBlockHut<?> hut
                && "town_hall".equals(hut.getBuildingId())) {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = new CompoundTag();
            BlockPos pos = context.getClickedPos();
            tag.putInt("thX", pos.getX());
            tag.putInt("thY", pos.getY());
            tag.putInt("thZ", pos.getZ());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            ((ServerPlayer) player).displayClientMessage(
                Component.literal("Noting requests on the clipboard from your colony.").withStyle(ChatFormatting.GOLD), false);
            return InteractionResult.CONSUME;
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

        if (!tag.contains("thX")) {
            sp.displayClientMessage(Component.literal("No colony set (sneak + use the clipboard on the Town Hall)").withStyle(ChatFormatting.RED), false);
            return InteractionResult.CONSUME;
        }

        // Show requests
        sp.displayClientMessage(Component.literal("=== Colony Requests ===").withStyle(ChatFormatting.GOLD), false);
        RequestManager rm = RequestManager.get(sl);
        int shown = 0;
        for (IRequest req : rm.getAllActiveRequests()) {
            if (req.getState() == RequestState.COMPLETED || req.getState() == RequestState.CANCELLED) continue;
            String state = switch (req.getState()) {
                case CREATED -> "\u00A7e[Pending]";
                case ASSIGNED -> "\u00A76[Assigned]";
                case IN_PROGRESS -> "\u00A7a[In Progress]";
                default -> "\u00A77[" + req.getState().name() + "]";
            };
            sp.displayClientMessage(Component.literal(state + " \u00A7f" + req.getRequestable().getDescription() + " \u00A77- " + req.getRequester().getRequesterName()), false);
            if (++shown >= 20) { sp.displayClientMessage(Component.literal("... and more").withStyle(ChatFormatting.GRAY), false); break; }
        }
        if (shown == 0) sp.displayClientMessage(Component.literal("No pending requests!").withStyle(ChatFormatting.GREEN), false);

        // Show work orders
        sp.displayClientMessage(Component.literal("=== Work Orders ===").withStyle(ChatFormatting.GOLD), false);
        int orders = 0;
        for (ColonyWorkOrder order : WorkManager.get(sl).getOrders()) {
            String status = order.isClaimed() ? "\u00A7a[Claimed]" : "\u00A7c[Unclaimed]";
            sp.displayClientMessage(Component.literal(status + " \u00A7f" + order.getBuildingId() + " L" + order.getTargetLevel() + " \u00A77(" + order.getType().name() + ")"), false);
            orders++;
        }
        if (orders == 0) sp.displayClientMessage(Component.literal("No work orders!").withStyle(ChatFormatting.GREEN), false);

        return InteractionResult.CONSUME;
    }
}
