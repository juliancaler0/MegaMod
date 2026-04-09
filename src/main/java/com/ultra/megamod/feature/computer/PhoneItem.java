package com.ultra.megamod.feature.computer;

import com.ultra.megamod.feature.computer.admin.AdminSystem;
import com.ultra.megamod.feature.computer.network.OpenComputerPayload;
import com.ultra.megamod.feature.economy.EconomyManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.function.Consumer;

public class PhoneItem extends Item {

    public PhoneItem(Item.Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Block block = level.getBlockState(pos).getBlock();

        if (block instanceof ComputerBlock) {
            if (!level.isClientSide() && context.getPlayer() instanceof ServerPlayer player) {
                // Link phone to this computer
                ItemStack stack = context.getItemInHand();
                CompoundTag tag = new CompoundTag();
                tag.putInt("linked_x", pos.getX());
                tag.putInt("linked_y", pos.getY());
                tag.putInt("linked_z", pos.getZ());
                tag.putString("linked_dim", level.dimension().identifier().toString());
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(true), List.of(), List.of()));
                player.sendSystemMessage(Component.literal("Phone linked to computer at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).withStyle(ChatFormatting.GREEN));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            boolean isAdmin = AdminSystem.isAdmin(serverPlayer);

            if (!tag.contains("linked_x") && !isAdmin) {
                serverPlayer.sendSystemMessage(Component.literal("Phone not linked! Right-click a Computer block first.").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }

            // Open computer GUI remotely
            EconomyManager eco = EconomyManager.get((ServerLevel) level);
            int wallet = eco.getWallet(player.getUUID());
            int bank = eco.getBank(player.getUUID());
            PacketDistributor.sendToPlayer(serverPlayer, new OpenComputerPayload(isAdmin, wallet, bank));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("linked_x")) {
            int x = tag.getIntOr("linked_x", 0);
            int y = tag.getIntOr("linked_y", 0);
            int z = tag.getIntOr("linked_z", 0);
            tooltip.accept(Component.literal("Linked to computer at " + x + ", " + y + ", " + z).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.accept(Component.literal("Not linked - right-click a Computer").withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.literal("Use to access computer remotely").withStyle(ChatFormatting.DARK_GRAY));
    }
}
