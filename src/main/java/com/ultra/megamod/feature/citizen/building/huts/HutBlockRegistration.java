package com.ultra.megamod.feature.citizen.building.huts;

import com.ultra.megamod.feature.citizen.building.ColonyBuildingRegistry;
import com.ultra.megamod.feature.citizen.building.buildings.*;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers all 50 colony hut blocks and their corresponding block items.
 * Called from {@link ColonyBuildingRegistry#init(IEventBus)}.
 */
public class HutBlockRegistration {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks("megamod");
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("megamod");

    private static BlockBehaviour.Properties hutProps() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(3.0f)
                .noOcclusion();
    }

    // ========================= Resource Production (7) =========================

    public static final DeferredBlock<BlockHutFarmer> HUT_FARMER =
            BLOCKS.registerBlock("hut_farmer", BlockHutFarmer::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_FARMER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_farmer", HUT_FARMER);

    public static final DeferredBlock<BlockHutMiner> HUT_MINER =
            BLOCKS.registerBlock("hut_miner", BlockHutMiner::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_MINER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_miner", HUT_MINER);

    public static final DeferredBlock<BlockHutLumberjack> HUT_LUMBERJACK =
            BLOCKS.registerBlock("hut_lumberjack", BlockHutLumberjack::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_LUMBERJACK_ITEM =
            ITEMS.registerSimpleBlockItem("hut_lumberjack", HUT_LUMBERJACK);

    public static final DeferredBlock<BlockHutFisherman> HUT_FISHERMAN =
            BLOCKS.registerBlock("hut_fisherman", BlockHutFisherman::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_FISHERMAN_ITEM =
            ITEMS.registerSimpleBlockItem("hut_fisherman", HUT_FISHERMAN);

    public static final DeferredBlock<BlockHutPlantation> HUT_PLANTATION =
            BLOCKS.registerBlock("hut_plantation", BlockHutPlantation::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_PLANTATION_ITEM =
            ITEMS.registerSimpleBlockItem("hut_plantation", HUT_PLANTATION);

    public static final DeferredBlock<BlockHutNetherWorker> HUT_NETHER_WORKER =
            BLOCKS.registerBlock("hut_nether_worker", BlockHutNetherWorker::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_NETHER_WORKER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_nether_worker", HUT_NETHER_WORKER);

    public static final DeferredBlock<BlockHutQuarry> HUT_QUARRY =
            BLOCKS.registerBlock("hut_quarry", BlockHutQuarry::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_QUARRY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_quarry", HUT_QUARRY);

    // ========================= Animal Husbandry (6) =========================

    public static final DeferredBlock<BlockHutShepherd> HUT_SHEPHERD =
            BLOCKS.registerBlock("hut_shepherd", BlockHutShepherd::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SHEPHERD_ITEM =
            ITEMS.registerSimpleBlockItem("hut_shepherd", HUT_SHEPHERD);

    public static final DeferredBlock<BlockHutCowboy> HUT_COWBOY =
            BLOCKS.registerBlock("hut_cowboy", BlockHutCowboy::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_COWBOY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_cowboy", HUT_COWBOY);

    public static final DeferredBlock<BlockHutChickenHerder> HUT_CHICKEN_HERDER =
            BLOCKS.registerBlock("hut_chicken_herder", BlockHutChickenHerder::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_CHICKEN_HERDER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_chicken_herder", HUT_CHICKEN_HERDER);

    public static final DeferredBlock<BlockHutSwineHerder> HUT_SWINE_HERDER =
            BLOCKS.registerBlock("hut_swine_herder", BlockHutSwineHerder::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SWINE_HERDER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_swine_herder", HUT_SWINE_HERDER);

    public static final DeferredBlock<BlockHutRabbitHutch> HUT_RABBIT_HUTCH =
            BLOCKS.registerBlock("hut_rabbit_hutch", BlockHutRabbitHutch::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_RABBIT_HUTCH_ITEM =
            ITEMS.registerSimpleBlockItem("hut_rabbit_hutch", HUT_RABBIT_HUTCH);

    public static final DeferredBlock<BlockHutBeekeeper> HUT_BEEKEEPER =
            BLOCKS.registerBlock("hut_beekeeper", BlockHutBeekeeper::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BEEKEEPER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_beekeeper", HUT_BEEKEEPER);

    // ========================= Crafting (19) =========================

    public static final DeferredBlock<BlockHutBaker> HUT_BAKER =
            BLOCKS.registerBlock("hut_baker", BlockHutBaker::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BAKER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_baker", HUT_BAKER);

    public static final DeferredBlock<BlockHutBlacksmith> HUT_BLACKSMITH =
            BLOCKS.registerBlock("hut_blacksmith", BlockHutBlacksmith::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BLACKSMITH_ITEM =
            ITEMS.registerSimpleBlockItem("hut_blacksmith", HUT_BLACKSMITH);

    public static final DeferredBlock<BlockHutStonemason> HUT_STONEMASON =
            BLOCKS.registerBlock("hut_stonemason", BlockHutStonemason::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_STONEMASON_ITEM =
            ITEMS.registerSimpleBlockItem("hut_stonemason", HUT_STONEMASON);

    public static final DeferredBlock<BlockHutSawmill> HUT_SAWMILL =
            BLOCKS.registerBlock("hut_sawmill", BlockHutSawmill::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SAWMILL_ITEM =
            ITEMS.registerSimpleBlockItem("hut_sawmill", HUT_SAWMILL);

    public static final DeferredBlock<BlockHutSmeltery> HUT_SMELTERY =
            BLOCKS.registerBlock("hut_smeltery", BlockHutSmeltery::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SMELTERY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_smeltery", HUT_SMELTERY);

    public static final DeferredBlock<BlockHutStoneSmeltery> HUT_STONE_SMELTERY =
            BLOCKS.registerBlock("hut_stone_smeltery", BlockHutStoneSmeltery::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_STONE_SMELTERY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_stone_smeltery", HUT_STONE_SMELTERY);

    public static final DeferredBlock<BlockHutCrusher> HUT_CRUSHER =
            BLOCKS.registerBlock("hut_crusher", BlockHutCrusher::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_CRUSHER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_crusher", HUT_CRUSHER);

    public static final DeferredBlock<BlockHutSifter> HUT_SIFTER =
            BLOCKS.registerBlock("hut_sifter", BlockHutSifter::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SIFTER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_sifter", HUT_SIFTER);

    public static final DeferredBlock<BlockHutCook> HUT_COOK =
            BLOCKS.registerBlock("hut_cook", BlockHutCook::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_COOK_ITEM =
            ITEMS.registerSimpleBlockItem("hut_cook", HUT_COOK);

    public static final DeferredBlock<BlockHutKitchen> HUT_KITCHEN =
            BLOCKS.registerBlock("hut_kitchen", BlockHutKitchen::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_KITCHEN_ITEM =
            ITEMS.registerSimpleBlockItem("hut_kitchen", HUT_KITCHEN);

    public static final DeferredBlock<BlockHutDyer> HUT_DYER =
            BLOCKS.registerBlock("hut_dyer", BlockHutDyer::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_DYER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_dyer", HUT_DYER);

    public static final DeferredBlock<BlockHutFletcher> HUT_FLETCHER =
            BLOCKS.registerBlock("hut_fletcher", BlockHutFletcher::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_FLETCHER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_fletcher", HUT_FLETCHER);

    public static final DeferredBlock<BlockHutGlassblower> HUT_GLASSBLOWER =
            BLOCKS.registerBlock("hut_glassblower", BlockHutGlassblower::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_GLASSBLOWER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_glassblower", HUT_GLASSBLOWER);

    public static final DeferredBlock<BlockHutConcreteMixer> HUT_CONCRETE_MIXER =
            BLOCKS.registerBlock("hut_concrete_mixer", BlockHutConcreteMixer::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_CONCRETE_MIXER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_concrete_mixer", HUT_CONCRETE_MIXER);

    public static final DeferredBlock<BlockHutComposter> HUT_COMPOSTER =
            BLOCKS.registerBlock("hut_composter", BlockHutComposter::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_COMPOSTER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_composter", HUT_COMPOSTER);

    public static final DeferredBlock<BlockHutFlorist> HUT_FLORIST =
            BLOCKS.registerBlock("hut_florist", BlockHutFlorist::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_FLORIST_ITEM =
            ITEMS.registerSimpleBlockItem("hut_florist", HUT_FLORIST);

    public static final DeferredBlock<BlockHutMechanic> HUT_MECHANIC =
            BLOCKS.registerBlock("hut_mechanic", BlockHutMechanic::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_MECHANIC_ITEM =
            ITEMS.registerSimpleBlockItem("hut_mechanic", HUT_MECHANIC);

    public static final DeferredBlock<BlockHutAlchemist> HUT_ALCHEMIST =
            BLOCKS.registerBlock("hut_alchemist", BlockHutAlchemist::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_ALCHEMIST_ITEM =
            ITEMS.registerSimpleBlockItem("hut_alchemist", HUT_ALCHEMIST);

    public static final DeferredBlock<BlockHutEnchanter> HUT_ENCHANTER =
            BLOCKS.registerBlock("hut_enchanter", BlockHutEnchanter::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_ENCHANTER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_enchanter", HUT_ENCHANTER);

    // ========================= Military (6) =========================

    public static final DeferredBlock<BlockHutBarracks> HUT_BARRACKS =
            BLOCKS.registerBlock("hut_barracks", BlockHutBarracks::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BARRACKS_ITEM =
            ITEMS.registerSimpleBlockItem("hut_barracks", HUT_BARRACKS);

    public static final DeferredBlock<BlockHutBarracksTower> HUT_BARRACKS_TOWER =
            BLOCKS.registerBlock("hut_barracks_tower", BlockHutBarracksTower::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BARRACKS_TOWER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_barracks_tower", HUT_BARRACKS_TOWER);

    public static final DeferredBlock<BlockHutGuardTower> HUT_GUARD_TOWER =
            BLOCKS.registerBlock("hut_guard_tower", BlockHutGuardTower::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_GUARD_TOWER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_guard_tower", HUT_GUARD_TOWER);

    public static final DeferredBlock<BlockHutArchery> HUT_ARCHERY =
            BLOCKS.registerBlock("hut_archery", BlockHutArchery::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_ARCHERY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_archery", HUT_ARCHERY);

    public static final DeferredBlock<BlockHutCombatAcademy> HUT_COMBAT_ACADEMY =
            BLOCKS.registerBlock("hut_combat_academy", BlockHutCombatAcademy::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_COMBAT_ACADEMY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_combat_academy", HUT_COMBAT_ACADEMY);

    public static final DeferredBlock<BlockHutGateHouse> HUT_GATE_HOUSE =
            BLOCKS.registerBlock("hut_gate_house", BlockHutGateHouse::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_GATE_HOUSE_ITEM =
            ITEMS.registerSimpleBlockItem("hut_gate_house", HUT_GATE_HOUSE);

    // ========================= Education (3) =========================

    public static final DeferredBlock<BlockHutLibrary> HUT_LIBRARY =
            BLOCKS.registerBlock("hut_library", BlockHutLibrary::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_LIBRARY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_library", HUT_LIBRARY);

    public static final DeferredBlock<BlockHutSchool> HUT_SCHOOL =
            BLOCKS.registerBlock("hut_school", BlockHutSchool::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_SCHOOL_ITEM =
            ITEMS.registerSimpleBlockItem("hut_school", HUT_SCHOOL);

    public static final DeferredBlock<BlockHutUniversity> HUT_UNIVERSITY =
            BLOCKS.registerBlock("hut_university", BlockHutUniversity::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_UNIVERSITY_ITEM =
            ITEMS.registerSimpleBlockItem("hut_university", HUT_UNIVERSITY);

    // ========================= Services (5) =========================

    public static final DeferredBlock<BlockHutHospital> HUT_HOSPITAL =
            BLOCKS.registerBlock("hut_hospital", BlockHutHospital::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_HOSPITAL_ITEM =
            ITEMS.registerSimpleBlockItem("hut_hospital", HUT_HOSPITAL);

    public static final DeferredBlock<BlockHutGraveyard> HUT_GRAVEYARD =
            BLOCKS.registerBlock("hut_graveyard", BlockHutGraveyard::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_GRAVEYARD_ITEM =
            ITEMS.registerSimpleBlockItem("hut_graveyard", HUT_GRAVEYARD);

    public static final DeferredBlock<BlockHutTavern> HUT_TAVERN =
            BLOCKS.registerBlock("hut_tavern", BlockHutTavern::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_TAVERN_ITEM =
            ITEMS.registerSimpleBlockItem("hut_tavern", HUT_TAVERN);

    public static final DeferredBlock<BlockHutDeliveryman> HUT_DELIVERYMAN =
            BLOCKS.registerBlock("hut_deliveryman", BlockHutDeliveryman::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_DELIVERYMAN_ITEM =
            ITEMS.registerSimpleBlockItem("hut_deliveryman", HUT_DELIVERYMAN);

    public static final DeferredBlock<BlockHutWarehouse> HUT_WAREHOUSE =
            BLOCKS.registerBlock("hut_warehouse", BlockHutWarehouse::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_WAREHOUSE_ITEM =
            ITEMS.registerSimpleBlockItem("hut_warehouse", HUT_WAREHOUSE);

    public static final DeferredBlock<BlockHutBuilder> HUT_BUILDER =
            BLOCKS.registerBlock("hut_builder", BlockHutBuilder::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_BUILDER_ITEM =
            ITEMS.registerSimpleBlockItem("hut_builder", HUT_BUILDER);

    // ========================= Core (4) =========================

    public static final DeferredBlock<BlockHutTownHall> HUT_TOWN_HALL =
            BLOCKS.registerBlock("hut_town_hall", BlockHutTownHall::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_TOWN_HALL_ITEM =
            ITEMS.registerSimpleBlockItem("hut_town_hall", HUT_TOWN_HALL);

    public static final DeferredBlock<BlockHutResidence> HUT_RESIDENCE =
            BLOCKS.registerBlock("hut_residence", BlockHutResidence::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_RESIDENCE_ITEM =
            ITEMS.registerSimpleBlockItem("hut_residence", HUT_RESIDENCE);

    public static final DeferredBlock<BlockHutMysticalSite> HUT_MYSTICAL_SITE =
            BLOCKS.registerBlock("hut_mystical_site", BlockHutMysticalSite::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> HUT_MYSTICAL_SITE_ITEM =
            ITEMS.registerSimpleBlockItem("hut_mystical_site", HUT_MYSTICAL_SITE);

    public static final DeferredBlock<BlockPostBox> POST_BOX =
            BLOCKS.registerBlock("post_box", BlockPostBox::new, HutBlockRegistration::hutProps);
    public static final DeferredItem<BlockItem> POST_BOX_ITEM =
            ITEMS.registerSimpleBlockItem("post_box", POST_BOX);

    /**
     * Registers all hut blocks and items with the mod event bus,
     * and registers building-to-hut mappings in ColonyBuildingRegistry.
     *
     * @param modBus the mod event bus
     */
    public static void registerAll(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);

        // Register building-to-hut block mappings
        // Resource Production
        ColonyBuildingRegistry.registerHut("farmer", BuildingFarmer::new, HUT_FARMER);
        ColonyBuildingRegistry.registerHut("miner", BuildingMiner::new, HUT_MINER);
        ColonyBuildingRegistry.registerHut("lumberjack", BuildingLumberjack::new, HUT_LUMBERJACK);
        ColonyBuildingRegistry.registerHut("fisherman", BuildingFisherman::new, HUT_FISHERMAN);
        ColonyBuildingRegistry.registerHut("plantation", BuildingPlantation::new, HUT_PLANTATION);
        ColonyBuildingRegistry.registerHut("nether_worker", BuildingNetherWorker::new, HUT_NETHER_WORKER);
        ColonyBuildingRegistry.registerHut("quarry", BuildingQuarry::new, HUT_QUARRY);

        // Animal Husbandry
        ColonyBuildingRegistry.registerHut("shepherd", BuildingShepherd::new, HUT_SHEPHERD);
        ColonyBuildingRegistry.registerHut("cowboy", BuildingCowboy::new, HUT_COWBOY);
        ColonyBuildingRegistry.registerHut("chicken_herder", BuildingChickenHerder::new, HUT_CHICKEN_HERDER);
        ColonyBuildingRegistry.registerHut("swine_herder", BuildingSwineHerder::new, HUT_SWINE_HERDER);
        ColonyBuildingRegistry.registerHut("rabbit_hutch", BuildingRabbitHutch::new, HUT_RABBIT_HUTCH);
        ColonyBuildingRegistry.registerHut("beekeeper", BuildingBeekeeper::new, HUT_BEEKEEPER);

        // Crafting
        ColonyBuildingRegistry.registerHut("baker", BuildingBaker::new, HUT_BAKER);
        ColonyBuildingRegistry.registerHut("blacksmith", BuildingBlacksmith::new, HUT_BLACKSMITH);
        ColonyBuildingRegistry.registerHut("stonemason", BuildingStonemason::new, HUT_STONEMASON);
        ColonyBuildingRegistry.registerHut("sawmill", BuildingSawmill::new, HUT_SAWMILL);
        ColonyBuildingRegistry.registerHut("smeltery", BuildingSmeltery::new, HUT_SMELTERY);
        ColonyBuildingRegistry.registerHut("stone_smeltery", BuildingStoneSmeltery::new, HUT_STONE_SMELTERY);
        ColonyBuildingRegistry.registerHut("crusher", BuildingCrusher::new, HUT_CRUSHER);
        ColonyBuildingRegistry.registerHut("sifter", BuildingSifter::new, HUT_SIFTER);
        ColonyBuildingRegistry.registerHut("cook", BuildingCook::new, HUT_COOK);
        ColonyBuildingRegistry.registerHut("kitchen", BuildingKitchen::new, HUT_KITCHEN);
        ColonyBuildingRegistry.registerHut("dyer", BuildingDyer::new, HUT_DYER);
        ColonyBuildingRegistry.registerHut("fletcher", BuildingFletcher::new, HUT_FLETCHER);
        ColonyBuildingRegistry.registerHut("glassblower", BuildingGlassblower::new, HUT_GLASSBLOWER);
        ColonyBuildingRegistry.registerHut("concrete_mixer", BuildingConcreteMixer::new, HUT_CONCRETE_MIXER);
        ColonyBuildingRegistry.registerHut("composter", BuildingComposter::new, HUT_COMPOSTER);
        ColonyBuildingRegistry.registerHut("florist", BuildingFlorist::new, HUT_FLORIST);
        ColonyBuildingRegistry.registerHut("mechanic", BuildingMechanic::new, HUT_MECHANIC);
        ColonyBuildingRegistry.registerHut("alchemist", BuildingAlchemist::new, HUT_ALCHEMIST);
        ColonyBuildingRegistry.registerHut("enchanter", BuildingEnchanter::new, HUT_ENCHANTER);

        // Military
        ColonyBuildingRegistry.registerHut("barracks", BuildingBarracks::new, HUT_BARRACKS);
        ColonyBuildingRegistry.registerHut("barracks_tower", BuildingBarracksTower::new, HUT_BARRACKS_TOWER);
        ColonyBuildingRegistry.registerHut("guard_tower", BuildingGuardTower::new, HUT_GUARD_TOWER);
        ColonyBuildingRegistry.registerHut("archery", BuildingArchery::new, HUT_ARCHERY);
        ColonyBuildingRegistry.registerHut("combat_academy", BuildingCombatAcademy::new, HUT_COMBAT_ACADEMY);
        ColonyBuildingRegistry.registerHut("gate_house", BuildingGateHouse::new, HUT_GATE_HOUSE);

        // Education
        ColonyBuildingRegistry.registerHut("library", BuildingLibrary::new, HUT_LIBRARY);
        ColonyBuildingRegistry.registerHut("school", BuildingSchool::new, HUT_SCHOOL);
        ColonyBuildingRegistry.registerHut("university", BuildingUniversity::new, HUT_UNIVERSITY);

        // Services
        ColonyBuildingRegistry.registerHut("hospital", BuildingHospital::new, HUT_HOSPITAL);
        ColonyBuildingRegistry.registerHut("graveyard", BuildingGraveyard::new, HUT_GRAVEYARD);
        ColonyBuildingRegistry.registerHut("tavern", BuildingTavern::new, HUT_TAVERN);
        ColonyBuildingRegistry.registerHut("deliveryman", BuildingDeliveryman::new, HUT_DELIVERYMAN);
        ColonyBuildingRegistry.registerHut("warehouse", BuildingWarehouse::new, HUT_WAREHOUSE);
        ColonyBuildingRegistry.registerHut("builder", BuildingBuilder::new, HUT_BUILDER);

        // Core
        ColonyBuildingRegistry.registerHut("town_hall", BuildingTownHall::new, HUT_TOWN_HALL);
        ColonyBuildingRegistry.registerHut("residence", BuildingResidence::new, HUT_RESIDENCE);
        ColonyBuildingRegistry.registerHut("mystical_site", BuildingMysticalSite::new, HUT_MYSTICAL_SITE);
        ColonyBuildingRegistry.registerHut("post_box", BuildingPostBox::new, POST_BOX);
    }
}
