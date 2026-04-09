package com.ultra.megamod.feature.citizen.building;

import com.ultra.megamod.feature.citizen.building.huts.HutBlockRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * DeferredRegister hub for the shared colony building block entity type,
 * and central point for building-to-hut mappings.
 * <p>
 * Block and item registration for hut blocks is handled by
 * {@link HutBlockRegistration}, which calls
 * {@link #registerHut(String, Supplier, DeferredBlock)} to register each
 * building type's factory and collect hut blocks for the shared block entity.
 * <p>
 * Follows the same pattern as {@link com.ultra.megamod.feature.citizen.block.TownChestRegistry}.
 */
public class ColonyBuildingRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            Registries.BLOCK_ENTITY_TYPE, "megamod");

    /**
     * Shared block entity type for all colony hut blocks.
     * Populated during {@link #init(IEventBus)} after all hut blocks are registered.
     */
    public static Supplier<BlockEntityType<TileEntityColonyBuilding>> COLONY_BUILDING_BE;

    /**
     * Collects all registered hut block suppliers so the shared block entity type
     * can reference them all.
     */
    private static final List<Supplier<? extends Block>> hutBlocks = new ArrayList<>();

    /**
     * Registers a building type mapping and collects the hut block for the shared BE type.
     * Called from {@link HutBlockRegistration#registerAll(IEventBus)} for each building type.
     * <p>
     * This method:
     * <ul>
     *   <li>Collects the hut block reference for the shared block entity type</li>
     *   <li>Registers a {@link BuildingEntry} in {@link BuildingRegistry}</li>
     * </ul>
     *
     * @param id              the building type ID (e.g., "farmer", "baker", "residence")
     * @param buildingFactory supplier that creates a new AbstractBuilding instance
     * @param hutBlock        the deferred block reference for this building's hut block
     */
    public static void registerHut(String id, Supplier<AbstractBuilding> buildingFactory,
                                   DeferredBlock<? extends AbstractBlockHut<?>> hutBlock) {
        hutBlocks.add(hutBlock);

        // Derive a display name from the building factory (create a temp instance)
        // We create the BuildingEntry with a null hutBlockFactory since the block
        // is already registered by HutBlockRegistration
        String displayName = capitalizeId(id);
        BuildingRegistry.register(new BuildingEntry(
                id,
                displayName,
                5,
                buildingFactory,
                () -> null  // hut block already registered externally
        ));
    }

    /**
     * Initializes the block entity register and delegates to
     * {@link HutBlockRegistration#registerAll(IEventBus)} for block/item registration.
     * Must be called from the main mod class during mod construction.
     *
     * @param modBus the mod event bus
     */
    public static void init(IEventBus modBus) {
        // Register all hut blocks and items first, which populates hutBlocks via registerHut()
        HutBlockRegistration.registerAll(modBus);

        // Register the shared block entity type
        BLOCK_ENTITIES.register(modBus);
        COLONY_BUILDING_BE = BLOCK_ENTITIES.register("colony_building",
                () -> new BlockEntityType<>(
                        TileEntityColonyBuilding::new,
                        hutBlocks.stream()
                                .map(Supplier::get)
                                .toArray(Block[]::new)
                ));
    }

    /**
     * Converts an underscore-separated ID into a display name.
     * e.g., "stone_smeltery" -> "Stone Smeltery"
     */
    private static String capitalizeId(String id) {
        String[] parts = id.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }
}
