package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Creative-only measurement tool. Measures distance between two clicked points.
 * First click sets point 1, second click calculates and displays measurements,
 * then clears point 1 for a new measurement.
 */
public class ItemCalipers extends Item {

    public ItemCalipers(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();

        // Check if pos1 is already stored
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        net.minecraft.nbt.CompoundTag tag = customData.copyTag();

        if (tag.contains("Pos1X")) {
            // Second click: calculate measurements
            int x1 = tag.getIntOr("Pos1X", 0);
            int y1 = tag.getIntOr("Pos1Y", 0);
            int z1 = tag.getIntOr("Pos1Z", 0);
            BlockPos pos1 = new BlockPos(x1, y1, z1);

            int dx = Math.abs(clickedPos.getX() - pos1.getX()) + 1;
            int dy = Math.abs(clickedPos.getY() - pos1.getY()) + 1;
            int dz = Math.abs(clickedPos.getZ() - pos1.getZ()) + 1;

            // Manhattan distance between the two points
            int manhattan = Math.abs(clickedPos.getX() - pos1.getX())
                + Math.abs(clickedPos.getY() - pos1.getY())
                + Math.abs(clickedPos.getZ() - pos1.getZ());
            double euclidean = Math.sqrt(
                Math.pow(clickedPos.getX() - pos1.getX(), 2)
                    + Math.pow(clickedPos.getY() - pos1.getY(), 2)
                    + Math.pow(clickedPos.getZ() - pos1.getZ(), 2));

            boolean sameY = pos1.getY() == clickedPos.getY();

            if (sameY && (dx == 1 || dz == 1)) {
                // Line measurement (1D along one axis on same Y)
                int lineLength = Math.max(dx, dz);
                player.displayClientMessage(Component.literal(
                    "Distance: " + manhattan + " blocks (diagonal: " + String.format("%.1f", euclidean) + ")"), false);
            } else if (sameY) {
                // Surface measurement (2D, same Y level)
                int area = dx * dz;
                player.displayClientMessage(Component.literal(
                    "Surface: " + dx + "x" + dz + " = " + area + " blocks"), false);
            } else {
                // Volume measurement (3D, different Y levels)
                int volume = dx * dy * dz;
                double diagonal = Math.sqrt(dx * dx + dy * dy + dz * dz);
                player.displayClientMessage(Component.literal(
                    "Volume: " + dx + "x" + dy + "x" + dz + " = " + volume + " blocks (diagonal: " + String.format("%.1f", diagonal) + ")"), false);
            }

            // Clear pos1
            tag.remove("Pos1X");
            tag.remove("Pos1Y");
            tag.remove("Pos1Z");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        } else {
            // First click: store pos1
            tag.putInt("Pos1X", clickedPos.getX());
            tag.putInt("Pos1Y", clickedPos.getY());
            tag.putInt("Pos1Z", clickedPos.getZ());
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

            player.displayClientMessage(Component.literal(
                "Point 1 set: " + clickedPos.getX() + ", " + clickedPos.getY() + ", " + clickedPos.getZ()), false);
        }

        return InteractionResult.SUCCESS;
    }
}
