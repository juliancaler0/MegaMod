package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Supply Camp Deployer item. When right-clicked:
 * <ul>
 *   <li>CLIENT: Opens the {@link WindowSupplies} screen for blueprint preview,
 *       style selection, rotation, and position adjustment.</li>
 *   <li>SERVER: Does nothing (placement is handled via {@code SupplyPlacePayload}).</li>
 * </ul>
 */
public class ItemSupplyCampDeployer extends Item {

    public ItemSupplyCampDeployer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            BlockPos clickedPos = context.getClickedPos();
            Direction face = context.getClickedFace();
            // Place the preview one block above the clicked surface
            BlockPos placePos = face == Direction.UP ? clickedPos.above() : clickedPos.relative(face);
            openSupplyScreen(placePos);
        }
        // Return SUCCESS on client to swing arm, CONSUME on server to prevent default behavior
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            // No block clicked -- use player position as initial placement
            BlockPos placePos = player.blockPosition();
            openSupplyScreen(placePos);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Opens the supply placement screen on the client side.
     * TODO: Port WindowSupplies to BOWindow system
     */
    private void openSupplyScreen(BlockPos placePos) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.displayClientMessage(Component.literal(
                "\u00A76[Colony] \u00A7eSupply Camp deployer placed at " + placePos.toShortString() + ". \u00A77Blueprint preview in development."), true);
        }
    }
}
