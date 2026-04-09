package com.ultra.megamod.feature.citizen.blueprint.network;

import com.mojang.logging.LogUtils;
import com.ultra.megamod.feature.citizen.blueprint.BlockInfo;
import com.ultra.megamod.feature.citizen.blueprint.Blueprint;
import com.ultra.megamod.feature.citizen.blueprint.RotationMirror;
import com.ultra.megamod.feature.citizen.blueprint.packs.StructurePacks;
import com.ultra.megamod.feature.citizen.building.TileEntityColonyBuilding;
import com.ultra.megamod.feature.citizen.building.huts.HutBlockRegistration;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.data.FactionData;
import com.ultra.megamod.feature.citizen.data.FactionManager;
import com.ultra.megamod.feature.citizen.data.SupplyPlacedTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

import java.util.List;

/**
 * Client -> Server payload: request placement of a supply camp or ship structure.
 * Sent when the player confirms placement in the WindowSupplies screen.
 */
public record SupplyPlacePayload(
        String packId,
        String blueprintPath,
        BlockPos position,
        int rotationMirrorOrdinal,
        String supplyType
) implements CustomPacketPayload {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final CustomPacketPayload.Type<SupplyPlacePayload> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("megamod", "supply_place"));

    public static final StreamCodec<FriendlyByteBuf, SupplyPlacePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SupplyPlacePayload decode(FriendlyByteBuf buf) {
                    return new SupplyPlacePayload(
                            buf.readUtf(128),
                            buf.readUtf(256),
                            buf.readBlockPos(),
                            buf.readByte(),
                            buf.readUtf(32)
                    );
                }

                @Override
                public void encode(FriendlyByteBuf buf, SupplyPlacePayload payload) {
                    buf.writeUtf(payload.packId(), 128);
                    buf.writeUtf(payload.blueprintPath(), 256);
                    buf.writeBlockPos(payload.position());
                    buf.writeByte(payload.rotationMirrorOrdinal());
                    buf.writeUtf(payload.supplyType(), 32);
                }
            };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Server-side handler for supply placement.
     */
    public static void handleOnServer(SupplyPlacePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

            ServerLevel level = (ServerLevel) serverPlayer.level();
            boolean isCamp = "camp".equals(payload.supplyType());
            boolean isShip = "ship".equals(payload.supplyType());

            if (!isCamp && !isShip) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76Invalid supply type."));
                return;
            }

            // 1. Check one-per-world limit
            SupplyPlacedTracker tracker = SupplyPlacedTracker.get(level);
            if (isCamp && tracker.isCampPlaced()) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76A Supply Camp has already been placed in this world!"));
                return;
            }
            if (isShip && tracker.isShipPlaced()) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76A Supply Ship has already been placed in this world!"));
                return;
            }

            // 2. Check the player is holding the correct supply item
            ItemStack heldItem = serverPlayer.getMainHandItem();
            boolean holdingCorrectItem = false;
            if (isCamp && heldItem.getItem() instanceof com.ultra.megamod.feature.citizen.colonyblocks.ItemSupplyCampDeployer) {
                holdingCorrectItem = true;
            } else if (isShip && heldItem.getItem() instanceof com.ultra.megamod.feature.citizen.colonyblocks.ItemSupplyShipDeployer) {
                holdingCorrectItem = true;
            }
            if (!holdingCorrectItem) {
                // Check offhand
                heldItem = serverPlayer.getOffhandItem();
                if (isCamp && heldItem.getItem() instanceof com.ultra.megamod.feature.citizen.colonyblocks.ItemSupplyCampDeployer) {
                    holdingCorrectItem = true;
                } else if (isShip && heldItem.getItem() instanceof com.ultra.megamod.feature.citizen.colonyblocks.ItemSupplyShipDeployer) {
                    holdingCorrectItem = true;
                }
            }
            if (!holdingCorrectItem) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76You must be holding the supply item to place it."));
                return;
            }

            // 3. Ensure packs are discovered on the server side, then load the blueprint
            if (StructurePacks.loadedPacks.isEmpty()) {
                java.nio.file.Path packsDir = java.nio.file.Path.of("blueprints", "megamod");
                StructurePacks.discoverPacks(packsDir);
            }
            Blueprint blueprint = StructurePacks.loadBlueprintFull(payload.packId(), payload.blueprintPath());
            if (blueprint == null) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76Failed to load blueprint: " + payload.blueprintPath()));
                return;
            }

            // 4. Determine rotation/mirror
            RotationMirror rm = RotationMirror.NONE;
            RotationMirror[] values = RotationMirror.values();
            if (payload.rotationMirrorOrdinal() >= 0 && payload.rotationMirrorOrdinal() < values.length) {
                rm = values[payload.rotationMirrorOrdinal()];
            }

            // 5. Validate placement area
            BlockPos placePos = payload.position();
            List<BlockInfo> blocks = blueprint.getBlockInfoAsList();
            if (blocks == null || blocks.isEmpty()) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76Blueprint contains no blocks."));
                return;
            }

            // Basic area check - ensure the area isn't submerged/filled with solid blocks
            int totalBlocks = 0;
            int obstructedBlocks = 0;
            BlockPos primaryOffset = blueprint.getPrimaryBlockOffset();
            for (BlockInfo info : blocks) {
                if (info.state() == null || info.state().isAir()) continue;
                BlockPos localTransformed = rm.applyToPos(info.pos().subtract(primaryOffset));
                BlockPos worldPos = placePos.offset(localTransformed);
                totalBlocks++;
                BlockState existing = level.getBlockState(worldPos);
                if (existing.isSolid() && !existing.canBeReplaced()) {
                    obstructedBlocks++;
                }
            }

            // Allow placement if less than 50% of non-air blocks are obstructed
            if (totalBlocks > 0 && (double) obstructedBlocks / totalBlocks > 0.50) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7l\u2716 \u00A76Too many solid blocks in the placement area! " +
                                obstructedBlocks + "/" + totalBlocks + " blocks obstructed."));
                return;
            }

            // 6. Create or get faction
            FactionManager fm = FactionManager.get(level);
            String factionId = fm.getPlayerFaction(serverPlayer.getUUID());
            if (factionId == null) {
                factionId = createFactionForPlayer(fm, serverPlayer, level);
                if (factionId == null) return;
            }

            // 7. Place all blocks from the blueprint
            int placed = 0;
            BlockPos townHallPos = null;
            for (BlockInfo info : blocks) {
                if (info.state() == null || info.state().isAir()) continue;

                BlockPos localTransformed = rm.applyToPos(info.pos().subtract(primaryOffset));
                BlockPos worldPos = placePos.offset(localTransformed);

                // Rotate/mirror the block state
                BlockState state = info.state();
                if (rm.getMirror() != Mirror.NONE) {
                    state = state.mirror(rm.getMirror());
                }
                if (rm.getRotation() != Rotation.NONE) {
                    state = state.rotate(rm.getRotation());
                }

                level.setBlock(worldPos, state, 3);

                // Restore block entity data if present
                if (info.hasTileEntityData()) {
                    CompoundTag beTag = info.tileEntityData().copy();
                    beTag.putInt("x", worldPos.getX());
                    beTag.putInt("y", worldPos.getY());
                    beTag.putInt("z", worldPos.getZ());
                    BlockEntity loaded = BlockEntity.loadStatic(worldPos, state, beTag, level.registryAccess());
                    if (loaded != null) {
                        level.setBlockEntity(loaded);
                    }
                }

                // Check if this is the Town Hall block
                if (state.getBlock() == HutBlockRegistration.HUT_TOWN_HALL.get()) {
                    townHallPos = worldPos;
                }

                placed++;
            }

            // 8. If no Town Hall was found in the blueprint, place one at the anchor position
            if (townHallPos == null) {
                townHallPos = placePos;
                BlockState townHallState = HutBlockRegistration.HUT_TOWN_HALL.get().defaultBlockState();
                level.setBlock(townHallPos, townHallState, 3);
                placed++;
            }

            // 9. Link the Town Hall block entity to the faction
            BlockEntity be = level.getBlockEntity(townHallPos);
            if (be instanceof TileEntityColonyBuilding tile) {
                tile.setBuildingId("town_hall");
                tile.setColonyId(serverPlayer.getUUID());
                tile.setChanged();
            }

            // 10. Claim the chunk
            ClaimManager cm = ClaimManager.get(level);
            int chunkX = placePos.getX() >> 4;
            int chunkZ = placePos.getZ() >> 4;
            boolean claimed = cm.claimChunk(factionId, chunkX, chunkZ);
            if (claimed) {
                cm.saveToDisk(level);
            }

            // 11. Mark supply as placed and save
            if (isCamp) {
                tracker.markCampPlaced(serverPlayer.getUUID());
            } else {
                tracker.markShipPlaced(serverPlayer.getUUID());
            }
            tracker.saveToDisk(level);

            // 12. Consume the item
            heldItem.shrink(1);

            // 13. Success message
            FactionData faction = fm.getFaction(factionId);
            String factionName = faction != null ? faction.getDisplayName() : factionId;
            String typeName = isCamp ? "Supply Camp" : "Supply Ship";
            serverPlayer.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7l\u2714 \u00A76" + typeName + " deployed! " + placed + " blocks placed at \u00A7f("
                            + placePos.getX() + ", " + placePos.getY() + ", " + placePos.getZ()
                            + ")\u00A76 for faction \u00A7f" + factionName + "\u00A76."));
            if (claimed) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A7a\u00A7l\u2714 \u00A76Chunk claimed for your faction."));
            }

            LOGGER.info("Player {} placed {} at ({}, {}, {}) from pack '{}' blueprint '{}'",
                    serverPlayer.getGameProfile().name(), typeName,
                    placePos.getX(), placePos.getY(), placePos.getZ(),
                    payload.packId(), payload.blueprintPath());
        });
    }

    /**
     * Auto-creates a faction for the player.
     */
    private static String createFactionForPlayer(FactionManager fm, ServerPlayer player, ServerLevel serverLevel) {
        String playerName = player.getGameProfile().name();
        String autoFactionId = playerName.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String autoDisplayName = playerName + "'s Colony";
        FactionData created = fm.createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
        if (created == null) {
            autoFactionId = autoFactionId + "_" + player.getUUID().toString().substring(0, 4);
            created = fm.createFaction(autoFactionId, autoDisplayName, player.getUUID(), 0);
        }
        if (created == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7c\u00A7l\u2716 \u00A76Failed to create faction. You may be banned from factions."));
            return null;
        }
        fm.saveToDisk(serverLevel);
        player.sendSystemMessage(Component.literal(
                "\u00A7a\u00A7l\u2714 \u00A76Faction '\u00A7f" + autoDisplayName + "\u00A76' created automatically!"));
        return autoFactionId;
    }
}
