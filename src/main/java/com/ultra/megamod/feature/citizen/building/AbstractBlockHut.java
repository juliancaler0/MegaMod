package com.ultra.megamod.feature.citizen.building;

import com.mojang.serialization.MapCodec;
import com.ultra.megamod.feature.citizen.CitizenManager;
import com.ultra.megamod.feature.citizen.CitizenRegistry;
import com.ultra.megamod.feature.citizen.data.CitizenJob;
import com.ultra.megamod.feature.citizen.data.ClaimManager;
import com.ultra.megamod.feature.citizen.colony.Colony;
import com.ultra.megamod.feature.citizen.colony.ColonyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Base block class for all colony hut blocks.
 * Each building type has a corresponding hut block that the player places in the world.
 * All hut blocks share a single {@link TileEntityColonyBuilding} block entity type.
 * <p>
 * Subclasses must override {@link #getBuildingId()} to return their building type ID
 * and provide their own {@link MapCodec} via {@link #codec()}.
 * <p>
 * Follows the same pattern as {@link com.ultra.megamod.feature.furniture.FurnitureBlock}
 * (extends HorizontalDirectionalBlock, has FACING property).
 *
 * @param <B> the concrete block subclass type (for codec self-reference)
 */
public abstract class AbstractBlockHut<B extends AbstractBlockHut<B>> extends HorizontalDirectionalBlock implements EntityBlock {

    protected AbstractBlockHut(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue((Property) FACING, (Comparable) Direction.NORTH));
    }

    // ==================== Abstract Methods ====================

    /**
     * Returns the building type ID this hut block represents (e.g., "residence", "baker").
     * Must match the corresponding {@link AbstractBuilding#getBuildingId()}.
     *
     * @return the building ID
     */
    public abstract String getBuildingId();

    /**
     * Each concrete subclass must provide its own MapCodec.
     * Use {@code simpleCodec(MyHutBlock::new)} for hut blocks with no extra block state.
     *
     * @return the codec for this block type
     */
    @Override
    protected abstract MapCodec<B> codec();

    // ==================== EntityBlock ====================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityColonyBuilding(pos, state);
    }

    // ==================== Interaction ====================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            // Open the building management screen on the client
            openBuildingScreen(pos);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    /**
     * Opens the building info screen on the client. Shows building type, level,
     * and owner info from the TileEntityColonyBuilding block entity.
     */
    private void openBuildingScreen(BlockPos pos) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Read building data from block entity
        String buildingId = getBuildingId();
        int level = 0;
        String ownerInfo = "";
        net.minecraft.world.level.block.entity.BlockEntity be = mc.level.getBlockEntity(pos);
        if (be instanceof TileEntityColonyBuilding tile) {
            level = tile.getBuildingLevel();
            String custom = tile.getCustomName();
            if (custom != null && !custom.isEmpty()) {
                buildingId = custom;
            }
        }

        // Format the building name nicely (e.g. "town_hall" -> "Town Hall")
        String displayName = buildingId.replace("_", " ");
        displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);
        // Capitalize after spaces
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : displayName.toCharArray()) {
            if (c == ' ') { capitalizeNext = true; sb.append(c); }
            else if (capitalizeNext) { sb.append(Character.toUpperCase(c)); capitalizeNext = false; }
            else { sb.append(c); }
        }
        displayName = sb.toString();

        String levelText = level > 0 ? " \u00A77(Level " + level + ")" : " \u00A77(Not built)";
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "\u00A76\u00A7l[Colony] \u00A7e" + displayName + levelText), false);
        mc.player.displayClientMessage(net.minecraft.network.chat.Component.literal(
            "\u00A77Position: " + pos.toShortString()), false);
    }

    // ==================== Block State ====================

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue((Property) FACING, (Comparable) ctx.getHorizontalDirection().getOpposite());
    }

    // ==================== Placement ====================

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.world.entity.LivingEntity placer,
                            net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide() || !(placer instanceof ServerPlayer player)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TileEntityColonyBuilding tile) {
            tile.setBuildingId(getBuildingId());

            ServerLevel serverLevel = (ServerLevel) level;
            ColonyManager cm = ColonyManager.get(serverLevel);
            String factionId = cm.getPlayerFaction(player.getUUID());

            // Auto-create a colony when placing the Town Hall if the player has none
            if (factionId == null && "town_hall".equals(getBuildingId())) {
                Colony colony = cm.createColonyForPlayer(player, serverLevel);
                if (colony != null) {
                    factionId = colony.getFactionId();
                    player.displayClientMessage(
                            Component.literal("\u00A7a\u00A7lColony '\u00A7f" + colony.getDisplayName() + "\u00A7a\u00A7l' created!"),
                            false);
                } else {
                    player.displayClientMessage(
                            Component.literal("\u00A7cFailed to create colony."),
                            false);
                }
            }

            if (factionId != null) {
                tile.setColonyId(player.getUUID());

                // Auto-claim chunks around the building based on building type
                int claimRadius = getClaimChunkRadius(getBuildingId(), 1);
                if (claimRadius > 0) {
                    ClaimManager claimMgr = ClaimManager.get(serverLevel);
                    int chunkX = pos.getX() >> 4;
                    int chunkZ = pos.getZ() >> 4;
                    for (int dx = -claimRadius; dx <= claimRadius; dx++) {
                        for (int dz = -claimRadius; dz <= claimRadius; dz++) {
                            claimMgr.claimChunk(factionId, chunkX + dx, chunkZ + dz);
                        }
                    }
                }

                // Register the building with the colony's building manager
                Colony colony = cm.getFaction(factionId);
                if (colony != null) {
                    colony.getBuildingManager().addBuilding(pos, getBuildingId(), 1);
                    colony.markDirty();
                }
            }

            tile.setChanged();

            // Spawn initial 4 citizens when Town Hall is first placed
            if ("town_hall".equals(getBuildingId()) && factionId != null) {
                spawnInitialCitizens(serverLevel, player, pos, factionId);
            }
        }
    }

    // Colony creation is now handled by ColonyManager.createColonyForPlayer()

    /**
     * Spawns the initial 4 citizens near the Town Hall when it is first placed.
     * These citizens appear immediately so the player can start building.
     * Each citizen is registered with {@link CitizenManager} so they appear in
     * colony management screens and faction citizen counts.
     */
    private static void spawnInitialCitizens(ServerLevel level, ServerPlayer player, BlockPos townHallPos, String factionId) {
        CitizenManager cm = CitizenManager.get(level);

        // Don't spawn if the colony already has citizens
        if (!cm.getCitizensForFaction(factionId).isEmpty()) return;

        int spawned = 0;
        for (int i = 0; i < 4; i++) {
            try {
                // Spawn MineColonies-style citizen (MCEntityCitizen with handler composition)
                var entityType = CitizenRegistry.MC_CITIZEN.get();
                var entity = entityType.create(level, EntitySpawnReason.EVENT);
                if (entity == null) continue;

                // Spread citizens around the Town Hall
                double angle = (Math.PI * 2.0 / 4) * i;
                double x = townHallPos.getX() + 0.5 + Math.cos(angle) * 3;
                double z = townHallPos.getZ() + 0.5 + Math.sin(angle) * 3;
                entity.setPos(x, townHallPos.getY() + 1, z);

                // Set gender and appearance first so name can match
                boolean isFemale = level.random.nextBoolean();
                entity.setFemale(isFemale);
                entity.setTextureId(level.random.nextInt(3));
                // Assign a random skin tone suffix (_a, _b, _d, _w)
                String[] suffixes = {"_a", "_b", "_d", "_w"};
                entity.setTextureSuffix(suffixes[level.random.nextInt(suffixes.length)]);

                // Generate a gender-appropriate name for this citizen
                String citizenName;
                var nameListener = com.ultra.megamod.feature.citizen.data.listener.CitizenNameListener.INSTANCE;
                if (nameListener != null) {
                    citizenName = nameListener.getRandomFullName("default", isFemale);
                } else {
                    citizenName = "Citizen " + (i + 1);
                }

                entity.setCitizenName(citizenName);
                entity.setPersistenceRequired();

                level.addFreshEntity(entity);

                // Register with CitizenManager so the citizen is tracked by the colony
                cm.registerCitizen(
                        entity.getUUID(),
                        player.getUUID(),
                        factionId,
                        CitizenJob.FARMER,
                        citizenName,
                        level.getGameTime()
                );

                spawned++;
            } catch (Exception e) {
                com.ultra.megamod.MegaMod.LOGGER.warn("Failed to spawn initial citizen {}", i, e);
            }
        }

        if (spawned > 0) {
            // Save citizen data immediately
            cm.saveToDisk(level);

            player.displayClientMessage(
                    Component.literal("\u00A7a\u00A7lColony Founded! \u00A77" + spawned
                            + " citizens have arrived at your Town Hall."),
                    false);
        }
    }

    // ==================== Claim Radius ====================

    /**
     * Returns the chunk claim radius for a building type at a given level.
     * Matching MegaColonies colony border specifications:
     *   Town Hall: scales 1→5 with level
     *   Guard Tower: scales 2→5 with level
     *   Barracks: fixed 2 at L1-3, 3 at L4-5
     *   All other buildings: 1 at L1-3, 2 at L4-5
     */
    public static int getClaimChunkRadius(String buildingId, int level) {
        return switch (buildingId) {
            case "town_hall" -> Math.max(1, Math.min(5, level));
            case "guard_tower" -> Math.max(2, Math.min(5, level + 1));
            case "barracks", "barracks_tower" -> level >= 4 ? 3 : 2;
            default -> level >= 4 ? 2 : 1;
        };
    }

    // ==================== Properties Helper ====================

    /**
     * Standard block properties for colony hut blocks.
     * Wood-like material, moderate hardness, no occlusion (for schematic rendering).
     *
     * @return default hut block properties
     */
    public static BlockBehaviour.Properties hutProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(2.0f)
                .noOcclusion();
    }
}
