package com.ultra.megamod.feature.citizen.colonyblocks;

import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Supply Ship Deployer item. When right-clicked:
 * <ul>
 *   <li>CLIENT: Opens the {@link WindowSupplies} screen for blueprint preview,
 *       style selection, rotation, and position adjustment (ship mode).</li>
 *   <li>SERVER: Does nothing (placement is handled via {@code SupplyPlacePayload}).</li>
 * </ul>
 */
public class ItemSupplyShipDeployer extends Item {

    public ItemSupplyShipDeployer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            BlockPos clickedPos = context.getClickedPos();
            // For ships, use the clicked position (should be on water surface)
            BlockPos placePos = clickedPos.above();
            openSupplyScreen(placePos);
        }
        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
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
                "\u00A76[Colony] \u00A7eSupply Ship deployer placed at " + placePos.toShortString() + ". \u00A77Blueprint preview in development."), true);
        }
    }
}
