package com.ultra.megamod.feature.dimensions.resource;

import com.ultra.megamod.feature.dimensions.DimensionHelper;
import com.ultra.megamod.feature.dimensions.DimensionRegistry;
import com.ultra.megamod.feature.dimensions.MegaModDimensions;
import com.ultra.megamod.feature.dimensions.PortalBlock;
import com.ultra.megamod.feature.toggles.FeatureToggleManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Consumer;

public class ResourceDimensionKeyItem extends Item {

    public ResourceDimensionKeyItem(Item.Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel overworld = serverPlayer.level().getServer().overworld();

        // Check feature toggle
        if (!FeatureToggleManager.get(overworld).isEnabled("resource_dimension")) {
            serverPlayer.sendSystemMessage(Component.literal("Resource Dimension is disabled.").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Check if player is already in the resource dimension
        if (serverPlayer.level().dimension().equals(MegaModDimensions.RESOURCE)) {
            serverPlayer.sendSystemMessage(Component.literal("You are already in the Resource Dimension!").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        ServerLevel resourceLevel = serverPlayer.level().getServer().getLevel(MegaModDimensions.RESOURCE);
        if (resourceLevel == null) {
            serverPlayer.sendSystemMessage(Component.literal("Resource Dimension is not available!").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        // Consume the key
        ItemStack stack = player.getItemInHand(hand);
        stack.shrink(1);

        // Get spawn position from manager
        ResourceDimensionManager manager = ResourceDimensionManager.get(overworld);
        BlockPos baseSpawn = manager.getSpawnPos();

        // Find surface Y at spawn position
        BlockPos surfacePos = resourceLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, baseSpawn);
        if (surfacePos.getY() <= resourceLevel.getMinY()) {
            // Fallback if heightmap returns invalid (chunk not generated yet)
            // Force-load the chunk first, then re-check
            resourceLevel.getChunk(baseSpawn);
            surfacePos = resourceLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, baseSpawn);
            if (surfacePos.getY() <= resourceLevel.getMinY()) {
                surfacePos = new BlockPos(baseSpawn.getX(), 70, baseSpawn.getZ());
            }
        }

        // Build a small 3x3 stone platform + portal at spawn
        buildSpawnPlatform(resourceLevel, surfacePos);

        // Play teleport sound
        level.playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.6f, 1.4f);

        // Teleport player
        DimensionHelper.teleportToDimension(serverPlayer, MegaModDimensions.RESOURCE, surfacePos.above(), 0.0f, 0.0f);

        serverPlayer.sendSystemMessage(Component.literal("Welcome to the Resource Dimension!").withStyle(ChatFormatting.GREEN));

        // Show time until reset
        long msRemaining = manager.getTimeUntilReset();
        long hoursRemaining = msRemaining / (1000 * 60 * 60);
        long minutesRemaining = (msRemaining / (1000 * 60)) % 60;
        serverPlayer.sendSystemMessage(Component.literal("Resets in " + hoursRemaining + "h " + minutesRemaining + "m").withStyle(ChatFormatting.GRAY));

        return InteractionResult.SUCCESS;
    }

    private void buildSpawnPlatform(ServerLevel level, BlockPos surfacePos) {
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState portalState = ((PortalBlock) DimensionRegistry.PORTAL_BLOCK.get()).defaultBlockState();

        // Build 3x3 platform at surface level
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos platformPos = surfacePos.offset(x, 0, z);
                level.setBlock(platformPos, stone, 3);
                // Clear 3 blocks above for headroom
                for (int y = 1; y <= 3; y++) {
                    level.setBlock(platformPos.above(y), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }

        // Place portal block (2 tall) at the edge of the platform
        BlockPos portalBase = surfacePos.offset(0, 1, -1);
        level.setBlock(portalBase, portalState, 3);
        level.setBlock(portalBase.above(), portalState, 3);

        // Frame around portal
        BlockState frame = Blocks.CHISELED_STONE_BRICKS.defaultBlockState();
        level.setBlock(portalBase.offset(-1, 0, 0), frame, 3);
        level.setBlock(portalBase.offset(1, 0, 0), frame, 3);
        level.setBlock(portalBase.offset(-1, 1, 0), frame, 3);
        level.setBlock(portalBase.offset(1, 1, 0), frame, 3);
        level.setBlock(portalBase.offset(-1, 2, 0), frame, 3);
        level.setBlock(portalBase.offset(0, 2, 0), frame, 3);
        level.setBlock(portalBase.offset(1, 2, 0), frame, 3);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal("Teleports you to the Resource Dimension").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.literal("A fresh overworld for gathering resources.").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.empty());
        tooltip.accept(Component.literal("Resets every 24 hours").withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("Single use").withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
