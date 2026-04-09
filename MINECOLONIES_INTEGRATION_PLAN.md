# MineColonies Full Integration Plan for MegaMod

## Executive Summary

Port 100% of MineColonies + Structurize + Domum Ornamentum + Multi-Piston into MegaMod as an enhanced colony system. MegaMod already has a 184-file citizen system with 13 worker types, 11 recruit types, factions, territory, sieges, economy, and a schematic builder. This plan upgrades that foundation with MineColonies' depth: 46 building types, 44 job types, modular building system, research trees, request/delivery system, 5 raider cultures, 22 building styles, combinatorial decorative blocks, and blueprint-based construction.

**Key Integration Decisions:**
- **Colony = Faction** — MegaMod factions become MineColonies-style colonies with all MC features
- **Keep MegaMod UI** — Use our Screen-based computer/phone UI, NOT BlockUI XML (adapt all MC windows to our pattern)
- **Keep MegaMod Economy** — MegaCoins stay; MC has no economy, so ours enriches theirs
- **Keep MegaMod Recruits** — Our 11 combat unit types + formations + groups stay alongside MC guards
- **Port Structurize into MegaMod** — Blueprint system becomes native, our existing SchematicLoader adapts to support .blueprint format alongside .schem
- **Keep existing schematic builder** — Players can still import schematics; MC-style building construction runs through the same BuildOrder system
- **Port Domum Ornamentum** — Architects Cutter + all combinatorial blocks become MegaMod blocks
- **Port Multi-Piston** — Single block addition with GUI

---

## Current MegaMod State (What We Have)

### Existing Colony System (~184 files)
- **13 Worker Types:** Farmer, Miner, Lumberjack, Fisherman, Shepherd, CattleFarmer, ChickenFarmer, SwineherdFarmer, RabbitFarmer, Beekeeper, GoatFarmer, Merchant, WarehouseWorker, Builder
- **11 Recruit Types:** Recruit, Shieldman, Bowman, Crossbowman, Nomad, Horseman, Commander, Captain, Messenger, Scout, Assassin
- **26 AI Goals:** Worker AI + combat AI + universal goals
- **Faction System:** FactionData, FactionManager, DiplomacyManager, TreatyManager, PlayerRank hierarchy
- **Territory System:** Chunk-based claims (max 50/faction), claim health, permissions
- **Siege System:** Attack/defend, health drain, territory transfer
- **Group/Formation System:** 10 formation types, squads, army management
- **Schematic System:** .schem loader, BuildOrder, BuildOrderManager, ghost renderer, material list, placement transforms
- **Economy:** Wallet + Bank + MegaShop + ATM + MegaCoins (integer)
- **Computer Colony App:** TownScreen with 9 tabs (Overview, Workers, Recruits, Groups, Diplomacy, Territory, Upkeep, War, Work Orders)
- **Persistence:** NbtIo per-manager .dat files in world/data/

### Existing Systems We Keep As-Is
- Dungeons, Casino, Backpacks, Alchemy, Relics, Ambient Sounds, Museum, HUD, Admin Modules, Bot Control, Skill Trees, Furniture, Corruption, Map, Arena, Recovery/Gravestone, Sorting, Prestige, Vanilla Refresh, Dynamic Lights, Computer apps (non-colony)

---

## What MineColonies Adds (Full Inventory)

### New Building Types to Port (33 new, 13 enhanced)

**Already have (enhance with MC modules):**
Farmer, Miner, Lumberjack (Forester), Fisherman, Shepherd, Cowboy (CattleFarmer), ChickenHerder, SwineHerder, RabbitHutch, Beekeeper, Warehouse, Builder, TownHall (our faction system)

**NEW buildings to add (33):**
1. Alchemist Laboratory (separate from our alchemy system — citizen-operated)
2. Archery (training building)
3. Bakery
4. Barracks (military housing — enhances our recruit system)
5. Barracks Tower
6. Blacksmith's Hut
7. Combat Academy (training)
8. Composter's Hut
9. Concrete Mixer's Hut
10. Chef's Kitchen
11. Courier's Hut (Deliveryman)
12. Crusher's Hut
13. Dyer's Hut
14. Enchanter's Tower
15. Fletcher's Hut
16. Flower Shop
17. Glassblower's Hut
18. Graveyard (citizen death management — complements our gravestone system)
19. Guard Tower
20. Hospital
21. Residence (citizen housing — we currently have no dedicated housing)
22. Library
23. Mechanic's Hut
24. Mystical Site
25. Nether Mine
26. Plantation
27. Quarry (Simple + Medium)
28. Dining Hall (Restaurant)
29. Sawmill
30. School
31. Sifter's Hut
32. Smeltery
33. Brick Yard (Stone Smeltery)
34. Stonemason's Hut
35. Tavern
36. University
37. GateHouse
38. PostBox
39. Stash

### New Job Types to Port (31 new, 13 enhanced)

**Already have (enhance):**
Farmer, Miner, Lumberjack, Fisherman, Shepherd, Cowboy, ChickenHerder, SwineHerder, RabbitHerder, Beekeeper, Builder, WarehouseWorker (→Deliveryman), Merchant (keep ours)

**NEW jobs to add:**
1. Alchemist (citizen)
2. Archer (training)
3. Baker
4. Blacksmith
5. Chef
6. Composter
7. Concrete Mixer
8. Cook
9. Crusher
10. Doctor/Healer
11. Druid
12. Dyer
13. Enchanter
14. Fletcher
15. Florist
16. Glassblower
17. Knight (Guard)
18. Mechanic
19. Nether Miner
20. Planter (Plantation)
21. Pupil
22. Quarrier
23. Researcher
24. Sifter
25. Smelter
26. Stone Smelter
27. Stonemason
28. Swineherd (enhance)
29. Teacher
30. Undertaker
31. Deliveryman/Courier

### New Blocks to Port (67 blocks)
- 54 Hut blocks (one per building type)
- Rack (storage), Stash, PostBox
- Barrel (fluid/item), Compost Barrel
- Scarecrow (farm field marker)
- Plantation Field
- Decoration Controller
- Construction Tape
- Waypoint
- Colony Flag Banner + Wall Banner
- Colony Sign
- Composted Dirt
- Grave + Named Grave
- Iron Gate + Wooden Gate
- 2 Farmland variants (regular + flooded)
- 15 Custom Crops (Durum, Eggplant, Garlic, Onion, Mint, Bell Pepper, Tomato, Corn, Cabbage, Butternut Squash, Chickpea, Soy Bean, Peas, Rice, Nether Pepper)

### New Items to Port (160+)
- 10 Scepters/Tools (Lumberjack, Permission, Guard, Beekeeper, Build Goggles, Scan Analyzer, Clipboard, etc.)
- 6 Scrolls (Colony TP, Area TP, Buff, Guard Help, Highlight, Resource)
- 2 Supply items (Supply Chest, Supply Camp)
- 5 Weapons (Chief Sword, Iron Scimitar, Pharao Scepter, Fire Arrow, Spear)
- Rally Banner
- 24 Armor pieces (Pirate x2, Plate, Santa Hat)
- 3 Assistant Hammers (Gold/Iron/Diamond)
- 4 Sifter Meshes (String/Flint/Iron/Diamond)
- 95+ Food items (3 tiers, biome-specific)
- 9 Dough/Ingredient items
- 4 Large Bottles (Water/Milk/Soy Milk/Empty)
- Colony Map, Ancient Tome, Compost, Mistletoe, Magic Potion, Quest Log, Adventure Token
- 18 Spawn Eggs (5 raider cultures x 3 tiers + 3 drowned)

### New Systems to Port

**1. Research System**
- University-driven tech tree
- Multiple research branches
- Research costs (items, scrolls)
- Research requirements (building levels, skills)
- Research effects (stat multipliers, recipe unlocks, building bonuses)
- Data-driven via JSON

**2. Request/Delivery System**
- Token-based async request tracking
- IRequestable → IRequest → IRequestResolver pipeline
- Deliveryman resolves delivery/pickup requests
- Player resolver for manual fulfillment
- Retrying resolver for automatic retry
- Buildings create requests, resolvers fulfill them
- PostBox as player interface to request system

**3. Building Module System**
- IAssignsJob — worker assignment
- IAssignsCitizen — housing
- ICraftingBuildingModule — recipe management
- ISettingsModule — building config
- IMinimumStockModule — inventory thresholds
- IEntityListModule — entity tracking
- ICreatesResolversModule — request resolvers
- 50+ specific module implementations

**4. Raider System (5 cultures)**
- Barbarians (Melee, Archer, Chief)
- Pirates (Melee, Archer, Captain) + Drowned variant
- Egyptians/Mummies (Melee, Archer, Pharaoh)
- Norsemen (Shieldmaiden, Archer, Chief)
- Amazons (Spearman, Archer, Chief)
- Dynamic difficulty scaling (1-14)
- Night-based raid scheduling
- Raider AI (melee, ranged, door breaking, pathing)
- Raid events with camp structure spawning

**5. Citizen Depth Enhancements**
- 11 Skills (Athletics, Strength, Dexterity, Agility, Stamina, Knowledge, Mana, Focus, Creativity, Adaptability, Intelligence) with complementary/adverse relationships
- Disease system (sickness, hospital treatment)
- Happiness system (housing, food, mourning, employment, security, research)
- Mourning system (grief on citizen death)
- Sleep system (bed assignment, day/night cycle)
- Food/Saturation system (hunger, starvation, food quality tiers)
- Citizen children (growth, education)
- Visitors (tavern, recruitment)

**6. Structurize Blueprint System**
- .blueprint format (NBT-based, palette + short[][][] structure)
- Scan Tool (region scanning → blueprint creation)
- Build Tool (ghost preview, placement, rotation/mirror)
- Shape Tool (procedural shapes: cube, sphere, pyramid, cylinder, cone, etc.)
- Tag Tool (position tagging in blueprints)
- Blueprint packs (22 styles with pack.json metadata)
- Placement handlers (20+ handlers for different block types)
- Build iterators (default layer-by-layer, Hilbert curve, spiral, random)
- Client-side ghost rendering (vertex buffer caching, fake level)
- Undo/redo support via ChangeStorage

**7. Domum Ornamentum (Combinatorial Blocks)**
- Architects Cutter crafting station
- Material texture data system (component → material mapping)
- 40+ block types with 100+ variants:
  - Timber Frames (10 types), Framed Lights (6 types)
  - Shingles (5 height variants) + Shingle Slabs
  - Pillars (round, voxel, square)
  - Doors, Fancy Doors, Trapdoors, Fancy Trapdoors
  - Panels, Posts, Paper Walls
  - Extra Blocks (27 variants), Barrels
  - Floating Carpets (16 colors)
  - All-Brick blocks, Brick variants (10 types)
  - Vanilla-compatible: Fence, FenceGate, Slab, Wall, Stair, Trapdoor
- Dynamic model baking (retextured quads, sprite transformation)
- Block entity texture storage + client sync
- 18 material tags for component compatibility

**8. Multi-Piston**
- Configurable block mover
- GUI for input/output direction, range (1-10), speed (1-3)
- Redstone-triggered push/pull
- Block entity movement with NBT preservation
- Entity displacement

**9. World Generation**
- Colony spawning in world (22 styles)
- Raider camp generation (5 cultures)
- Supply ship structures
- Template pools (jigsaw-based)
- Processor lists for block replacement

**10. Commands (60+)**
- Colony management (/colony info, list, teleport, claim, delete, etc.)
- Citizen commands (/citizen info, list, modify, kill, spawn, teleport, etc.)
- Kill commands (/kill monsters, animals, raiders, specific animals)
- General commands (/backup, /whereami, /whoami, etc.)
- Debug commands

**11. Enchantments**
- Raider Damage enchantment (bonus damage to raiders)

---

## Phase Breakdown

### PHASE 0: Structurize Blueprint System Port
**Priority: CRITICAL — Foundation for everything else**
**Estimated files: ~60**

This must come first because all MineColonies buildings use blueprints.

#### 0.1 Blueprint Core
Package: `com.ultra.megamod.feature.citizen.blueprint`

| File | Purpose | Source Reference |
|------|---------|-----------------|
| `Blueprint.java` | Core blueprint class — palette + short[][][] + entities + tile entities | Structurize `blueprints/v1/Blueprint.java` |
| `BlueprintUtil.java` | .blueprint file I/O (NBT read/write with compression) | Structurize `blueprints/v1/BlueprintUtil.java` |
| `BlueprintUtils.java` | Scan world region → blueprint, instantiate entities | Structurize `blueprints/v1/BlueprintUtils.java` |
| `BlockInfo.java` | Record: BlockPos + BlockState + CompoundTag | Structurize `util/BlockInfo.java` |
| `RotationMirror.java` | 8-state enum (4 rotations × 2 mirrors) | Structurize `util/RotationMirror.java` |
| `PlacementSettings.java` | Rotation, mirror, ground style config | Structurize `util/PlacementSettings.java` |
| `DataFixerUtils.java` | MC version data migration | Structurize `blueprints/v1/DataFixerUtils.java` |

#### 0.2 Blueprint Tools
Package: `com.ultra.megamod.feature.citizen.blueprint.tools`

| File | Purpose |
|------|---------|
| `ScanToolItem.java` | Mark two corners → scan region → save .blueprint |
| `ScanToolData.java` | NBT data for 10 scan slots on item |
| `BuildToolItem.java` | Place blueprints with preview, rotation, mirror |
| `ShapeToolItem.java` | Procedural shapes (cube, sphere, pyramid, cylinder, cone, etc.) |
| `TagToolItem.java` | Tag positions in blueprints |
| `Shape.java` | Enum: CUBE, SPHERE, HALF_SPHERE, BOWL, WAVE, DIAMOND, PYRAMID, CYLINDER, CONE |

#### 0.3 Placement System
Package: `com.ultra.megamod.feature.citizen.blueprint.placement`

| File | Purpose |
|------|---------|
| `StructurePlacer.java` | Core placement executor — iterate + handler chain |
| `IPlacementHandler.java` | Interface for block placement logic |
| `PlacementHandlers.java` | 20+ handlers: Air, Door, Bed, DoublePlant, Fire, Banner, Container, General, etc. |
| `IBlueprintIterator.java` | Iteration strategy interface |
| `BlueprintIteratorDefault.java` | Layer-by-layer (Y→Z→X) |
| `BlueprintIteratorHilbert.java` | Space-filling curve |
| `BlueprintIteratorInwardCircle.java` | Spiral outward |
| `IStructureHandler.java` | Placement context (blueprint, world, inventory) |
| `AbstractStructureHandler.java` | Base implementation |
| `CreativeStructureHandler.java` | Creative mode (unlimited items) |
| `ChangeStorage.java` | Undo/redo block state tracking |

#### 0.4 Blueprint Rendering (Client)
Package: `com.ultra.megamod.feature.citizen.blueprint.client`

| File | Purpose |
|------|---------|
| `BlueprintRenderer.java` | Ghost block preview rendering with vertex buffers |
| `BlueprintBlockAccess.java` | Fake level wrapping blueprint for rendering |
| `FakeLevel.java` | Minimal Level mock for blueprint block access |
| `FakeChunk.java` | Fake chunk for rendering system |
| `FakeLevelChunkSection.java` | Fake chunk section with palette |
| `FakeLevelLightEngine.java` | Configurable lighting for preview |

#### 0.5 Pack System
Package: `com.ultra.megamod.feature.citizen.blueprint.packs`

| File | Purpose |
|------|---------|
| `StructurePacks.java` | Central pack management + loading |
| `StructurePackMeta.java` | Pack metadata (name, path, version, blueprints) |
| `ServerStructurePackLoader.java` | Server-side pack loading from disk |
| `ClientStructurePackLoader.java` | Client-side pack caching |

#### 0.6 Network
Integrate into existing `ComputerNetwork` or create `BlueprintNetwork`:

| Payload | Direction | Purpose |
|---------|-----------|---------|
| `BlueprintSyncPayload` | S→C | Send blueprint to client |
| `BlueprintRequestPayload` | C→S | Request blueprint from server |
| `BuildToolPlacementPayload` | C→S | Execute placement |
| `SaveScanPayload` | C→S | Save scan to file |
| `UndoRedoPayload` | C→S | Undo/redo operations |

#### 0.7 Integration with Existing Schematic System
- Keep `SchematicLoader.java` for .schem format support
- Add `BlueprintLoader.java` adapter that converts .blueprint → our `SchematicData`
- `BuilderAI.java` enhanced to use either format
- `BuildOrder.java` enhanced with blueprint reference + iterator position
- Ghost renderer unified between old and new systems

#### 0.8 Screens
Package: `com.ultra.megamod.feature.citizen.blueprint.screen`

| File | Purpose |
|------|---------|
| `ScanToolScreen.java` | Configure scan parameters |
| `BuildToolScreen.java` | Blueprint browser, placement config |
| `ShapeToolScreen.java` | Shape parameters |
| `TagToolScreen.java` | Tag management |
| `PackBrowserScreen.java` | Browse style packs |

---

### PHASE 1: Building Module System & Core Buildings
**Priority: HIGH — Enables all MineColonies buildings**
**Estimated files: ~120**

#### 1.1 Building Module Framework
Package: `com.ultra.megamod.feature.citizen.building.module`

Port the modular building composition system:

| File | Purpose |
|------|---------|
| `IBuildingModule.java` | Base module interface |
| `IPersistentModule.java` | NBT persistence capability |
| `ITickingModule.java` | Per-tick logic capability |
| `IAssignsJob.java` | Worker assignment capability |
| `IAssignsCitizen.java` | Housing capability |
| `ICraftingBuildingModule.java` | Recipe management capability |
| `ISettingsModule.java` | Settings storage capability |
| `IMinimumStockModule.java` | Inventory threshold tracking |
| `IEntityListModule.java` | Entity tracking capability |
| `ICreatesResolversModule.java` | Request resolver creation |
| `IItemListModule.java` | Item filter list |
| `IDefinesCoreBuildingStatsModule.java` | Level/skill requirements |
| `IAltersBuildingFootprint.java` | Dynamic footprint |
| `IBuildingEventsModule.java` | Event hooks |

Module Settings:
| File | Purpose |
|------|---------|
| `ISetting.java` | Setting interface |
| `BoolSetting.java` | Boolean toggle |
| `IntSetting.java` | Integer value |
| `StringSetting.java` | String value |
| `BlockSetting.java` | Block selection |
| `RecipeSetting.java` | Recipe selection |
| `CraftingSetting.java` | Crafting config |

#### 1.2 Building Base Enhancement
Enhance `com.ultra.megamod.feature.citizen.building`:

| File | Purpose |
|------|---------|
| `AbstractBuilding.java` | ENHANCED — Add module system, work orders, request integration, level system (1-5), schematic provider |
| `BuildingRegistry.java` | Registry of all building types with BuildingEntry pattern |
| `BuildingEntry.java` | Building type definition (producer, modules, key) |
| `IBuildingContainer.java` | Hierarchical building interface |
| `ISchematicProvider.java` | Blueprint reference per building level |

#### 1.3 Hut Blocks
Package: `com.ultra.megamod.feature.citizen.building.blocks`

One block class per building type (54 total). Each extends `AbstractBlockHut`:

```
AbstractBlockHut.java — Base hut block with codec, block entity creation
BlockHutTownHall, BlockHutBuilder, BlockHutCitizen (Residence),
BlockHutWareHouse, BlockHutFarmer, BlockHutMiner, BlockHutLumberjack,
BlockHutFisherman, BlockHutShepherd, BlockHutCowboy, BlockHutChickenHerder,
BlockHutSwineHerder, BlockHutRabbitHutch, BlockHutBeekeeper,
BlockHutBaker, BlockHutBlacksmith, BlockHutStonemason, BlockHutSawmill,
BlockHutSmeltery, BlockHutStoneSmeltery, BlockHutCrusher, BlockHutSifter,
BlockHutCook, BlockHutKitchen, BlockHutDyer, BlockHutFletcher,
BlockHutGlassblower, BlockHutConcreteMixer, BlockHutComposter,
BlockHutFlorist, BlockHutMechanic, BlockHutAlchemist, BlockHutEnchanter,
BlockHutPlantation, BlockHutNetherWorker, BlockHutBarracks,
BlockHutBarracksTower, BlockHutGuardTower, BlockHutArchery,
BlockHutCombatAcademy, BlockHutGateHouse, BlockHutLibrary,
BlockHutSchool, BlockHutUniversity, BlockHutHospital,
BlockHutGraveyard, BlockHutMysticalSite, BlockHutTavern,
BlockHutDeliveryman, BlockHutQuarry (Simple+Medium),
BlockPostBox, BlockStash
```

Block Entity: `TileEntityColonyBuilding.java` — Shared block entity for all huts

#### 1.4 Work Order System Enhancement
Package: `com.ultra.megamod.feature.citizen.building.workorder`

| File | Purpose |
|------|---------|
| `IWorkOrder.java` | Work order interface |
| `WorkOrderBuild.java` | Build new building order |
| `WorkOrderUpgrade.java` | Upgrade building order |
| `WorkOrderRepair.java` | Repair building order |
| `WorkOrderDecoration.java` | Place decoration order |
| `WorkManager.java` | ENHANCED — Priority queue, builder assignment |

#### 1.5 All Building Implementations (46)
Package: `com.ultra.megamod.feature.citizen.building.buildings`

Each building class defines:
- Module composition (which modules it uses)
- Max level (typically 5)
- Worker types it houses
- Special mechanics

**Resource Production:**
`BuildingFarmer`, `BuildingMiner`, `BuildingLumberjack`, `BuildingFisherman`, `BuildingPlantation`, `BuildingNetherWorker`, `BuildingQuarry`

**Animal Husbandry:**
`BuildingShepherd`, `BuildingCowboy`, `BuildingChickenHerder`, `BuildingSwineHerder`, `BuildingRabbitHutch`, `BuildingBeekeeper`

**Crafting:**
`BuildingBaker`, `BuildingBlacksmith`, `BuildingStonemason`, `BuildingSawmill`, `BuildingSmeltery`, `BuildingStoneSmeltery`, `BuildingCrusher`, `BuildingSifter`, `BuildingCook`, `BuildingKitchen`, `BuildingDyer`, `BuildingFletcher`, `BuildingGlassblower`, `BuildingConcreteMixer`, `BuildingComposter`, `BuildingFlorist`, `BuildingMechanic`, `BuildingAlchemist`, `BuildingEnchanter`

**Military:**
`BuildingBarracks`, `BuildingBarracksTower`, `BuildingGuardTower`, `BuildingArchery`, `BuildingCombatAcademy`, `BuildingGateHouse`

**Education:**
`BuildingLibrary`, `BuildingSchool`, `BuildingUniversity`

**Services:**
`BuildingHospital`, `BuildingGraveyard`, `BuildingTavern`, `BuildingDeliveryman`, `BuildingWarehouse`

**Core:**
`BuildingTownHall`, `BuildingResidence`, `BuildingMysticalSite`, `PostBox`, `Stash`

---

### PHASE 2: Request/Delivery System
**Priority: HIGH — Core MC mechanic, many buildings depend on it**
**Estimated files: ~40**

#### 2.1 Request System Core
Package: `com.ultra.megamod.feature.citizen.request`

| File | Purpose |
|------|---------|
| `IRequestManager.java` | Creates/assigns/tracks requests |
| `StandardRequestManager.java` | Main implementation |
| `IRequest.java` | Request wrapper with state machine |
| `RequestState.java` | Enum: CREATED, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELED |
| `IToken.java` | Unique request identifier |
| `StandardToken.java` | UUID-based token |
| `IRequestable.java` | What's being requested |
| `IRequester.java` | Who's requesting |
| `IRequestResolver.java` | Who fulfills requests |

#### 2.2 Request Types
| File | Purpose |
|------|---------|
| `DeliveryRequest.java` | Item needs delivery to building |
| `PickupRequest.java` | Item needs pickup from building |
| `CraftingRequest.java` | Recipe needs crafting |
| `FoodRequest.java` | Citizen needs food |
| `ToolRequest.java` | Worker needs tool |
| `BuildingMaterialRequest.java` | Builder needs materials |

#### 2.3 Resolvers
| File | Purpose |
|------|---------|
| `DeliverymanResolver.java` | Deliveryman NPC fulfills delivery |
| `PlayerResolver.java` | Player manually fulfills request |
| `RetryingResolver.java` | Auto-retry on failure |
| `WarehouseResolver.java` | Pull from warehouse stock |
| `CraftingResolver.java` | Craft the requested item |

#### 2.4 Integration
- Buildings implement `IRequester`
- CitizenData references active requests
- PostBox block provides player interface
- Deliveryman job AI moves items between buildings
- Warehouse serves as central storage hub

---

### PHASE 3: Worker Expansion (31 New Jobs)
**Priority: HIGH — The heart of MineColonies gameplay**
**Estimated files: ~90**

#### 3.1 Citizen Skill Enhancement
Package: `com.ultra.megamod.feature.citizen.entity.skill`

Replace/enhance current simple skill system with MC's 11 skills:

| Skill | Complementary | Adverse |
|-------|--------------|---------|
| Athletics | Strength | Dexterity |
| Strength | Athletics | Agility |
| Dexterity | Agility | Athletics |
| Agility | Dexterity | Strength |
| Stamina | Knowledge | Mana |
| Knowledge | Stamina | Creativity |
| Mana | Focus | Stamina |
| Focus | Mana | Adaptability |
| Creativity | Adaptability | Knowledge |
| Adaptability | Creativity | Focus |
| Intelligence | — | — |

Each job uses primary + secondary skill affecting work speed/quality.

#### 3.2 Citizen Handler Enhancements
Package: `com.ultra.megamod.feature.citizen.entity.handler`

| File | Purpose |
|------|---------|
| `CitizenSkillHandler.java` | ENHANCED — 11 skills with XP, leveling, complementary/adverse |
| `CitizenFoodHandler.java` | NEW — Saturation 0-20, food quality tiers, starvation |
| `CitizenDiseaseHandler.java` | NEW — Sickness, hospital treatment |
| `CitizenHappinessHandler.java` | ENHANCED — Housing, food, mourning, employment, security, research modifiers |
| `CitizenMournHandler.java` | NEW — Grief on citizen death, happiness impact |
| `CitizenSleepHandler.java` | ENHANCED — Bed assignment, day/night cycle, health restore |
| `CitizenExperienceHandler.java` | ENHANCED — Skill XP, level-up system |
| `CitizenInventoryHandler.java` | ENHANCED — Request system integration |

#### 3.3 New Job Implementations
Package: `com.ultra.megamod.feature.citizen.entity.job`

Each job needs: Job class + AI class + building integration

**Crafting Jobs (16 new):**

| Job | Building | Primary/Secondary Skill | Products |
|-----|----------|------------------------|----------|
| Baker | Bakery | Knowledge/Dexterity | Bread, pastries, doughs |
| Blacksmith | Blacksmith Hut | Strength/Focus | Tools, weapons, armor |
| Chef | Kitchen | Adaptability/Knowledge | Prepared meals (Tier 2-3) |
| Composter | Composter Hut | Stamina/Athletics | Compost, composted dirt |
| ConcreteMixer | Concrete Mixer | Stamina/Dexterity | Concrete blocks |
| Cook | Cook Hut | Adaptability/Creativity | Basic cooked food (Tier 1) |
| Crusher | Crusher Hut | Strength/Stamina | Crushed ores/materials |
| Dyer | Dyer Hut | Creativity/Dexterity | Dyed blocks/items |
| Fletcher | Fletcher Hut | Dexterity/Creativity | Arrows, bows |
| Glassblower | Glassblower Hut | Creativity/Focus | Glass items |
| Mechanic | Mechanic Hut | Knowledge/Dexterity | Redstone items, mechanisms |
| Sawmill | Sawmill | Strength/Focus | Planks, wooden items |
| Sifter | Sifter Hut | Focus/Strength | Sifted ore (mesh durability) |
| Smelter | Smeltery | Strength/Focus | Smelted metals |
| StoneSmelter | Brick Yard | Strength/Focus | Smelted stone |
| Stonemason | Stonemason Hut | Creativity/Dexterity | Stone blocks |

**Service Jobs (6 new):**

| Job | Building | Products/Actions |
|-----|----------|-----------------|
| Deliveryman | Courier Hut | Item delivery between buildings |
| Enchanter | Enchanter Tower | Item enchanting |
| Healer | Hospital | Cure diseases, heal citizens |
| Undertaker | Graveyard | Manage graves, resurrect chance |
| Florist | Flower Shop | Grow/arrange flowers |
| Alchemist (citizen) | Alchemist Lab | Brew potions |

**Education Jobs (4 new):**

| Job | Building | Actions |
|-----|----------|---------|
| Pupil | School | Learn skills (children) |
| Teacher | School | Teach pupils |
| Student | University | Study at university |
| Researcher | University | Conduct research |

**Resource Jobs (4 new):**

| Job | Building | Actions |
|-----|----------|---------|
| NetherMiner | Nether Mine | Gather nether resources |
| Planter | Plantation | Grow plantation plants (12+ modules) |
| Quarrier | Quarry | Mine stone/ore in quarry |
| Druid | Guard Tower | Nature magic guard |

**Military Jobs (1 new + enhance existing):**

| Job | Building | Actions |
|-----|----------|---------|
| Knight | Barracks/Guard Tower | MC-style guard (integrates with our recruit system) |

#### 3.4 Worker AI Implementations
Package: `com.ultra.megamod.feature.citizen.entity.ai.worker`

Each new job gets a corresponding AI class. Key patterns from MineColonies:

- **Crafting AI Base**: `AbstractEntityAICrafting` — recipe lookup, ingredient gathering, crafting execution, output delivery
- **Smelting AI Base**: `AbstractEntityAIRequestSmelter` — furnace usage, fuel management
- **Herder AI Base**: `AbstractEntityAIHerder` — animal breeding, shearing, butchering (enhance existing)
- **Guard AI Base**: `AbstractEntityAIGuard` — patrol, defend, threat table

---

### PHASE 4: Research System
**Priority: MEDIUM-HIGH — Key progression mechanic**
**Estimated files: ~25**

Package: `com.ultra.megamod.feature.citizen.research`

#### 4.1 Research Framework
| File | Purpose |
|------|---------|
| `IResearchManager.java` | Colony research management |
| `ResearchManager.java` | Implementation with NBT persistence |
| `IGlobalResearchTree.java` | All possible researches (data-driven) |
| `GlobalResearchTree.java` | Loaded from JSON datapacks |
| `ILocalResearchTree.java` | Colony's research progress |
| `LocalResearchTree.java` | Tracks completed/in-progress |
| `LocalResearch.java` | Individual research progress (state, timer) |
| `ResearchState.java` | Enum: NOT_STARTED, IN_PROGRESS, FINISHED |
| `IResearchEffect.java` | Effect interface |
| `ResearchEffectManager.java` | Active effects on colony |
| `IResearchCost.java` | Cost interface (items, scrolls) |
| `IResearchRequirement.java` | Requirement interface (building level, parent research) |
| `ResearchBranch.java` | Research branch definition |

#### 4.2 Research Data
- JSON-driven research definitions in `data/megamod/research/`
- Branches: Combat, Civilian, Technology (mapped from MC's tree)
- Effects modify: worker speed, building bonuses, recipe unlocks, guard strength, citizen stats

#### 4.3 Integration
- University building triggers research
- Researcher job processes research
- Effects applied colony-wide via ResearchEffectManager
- Computer TownScreen gets new "Research" tab

---

### PHASE 5: Raider System
**Priority: MEDIUM — Combat content**
**Estimated files: ~50**

#### 5.1 Raider Entities (5 cultures × 3 tiers = 15 + 3 drowned)
Package: `com.ultra.megamod.feature.citizen.entity.raider`

| Culture | Melee | Ranged | Leader |
|---------|-------|--------|--------|
| Barbarian | EntityBarbarian | EntityArcherBarbarian | EntityChiefBarbarian |
| Pirate | EntityPirate | EntityArcherPirate | EntityCaptainPirate |
| Egyptian | EntityMummy | EntityArcherMummy | EntityPharao |
| Norsemen | EntityShieldmaiden | EntityNorsemenArcher | EntityNorsemenChief |
| Amazon | EntityAmazonSpearman | EntityArcherAmazon | EntityAmazonChief |
| DrownedPirate | EntityDrownedPirate | EntityDrownedArcherPirate | EntityDrownedCaptainPirate |

#### 5.2 Raider AI
| File | Purpose |
|------|---------|
| `RaiderMeleeAI.java` | Melee combat behavior |
| `RaiderRangedAI.java` | Ranged combat behavior |
| `RaiderWalkAI.java` | Navigation toward colony |
| `EntityAIBreakDoor.java` | Door breaking behavior |
| `CampWalkAI.java` | Camp idle behavior |

#### 5.3 Raid Events Enhancement
Enhance existing `ColonyRaidManager`:

| File | Purpose |
|------|---------|
| `RaidManager.java` | ENHANCED — 5 cultures, difficulty 1-14, night scheduling |
| `BarbarianRaidEvent.java` | Land-based barbarian attack |
| `PirateRaidEvent.java` | Ship-based pirate attack |
| `EgyptianRaidEvent.java` | Desert culture attack |
| `NorsemenRaidEvent.java` | Norse attack + ship variant |
| `AmazonRaidEvent.java` | Jungle ambush |
| `RaidDifficultyScaler.java` | Dynamic difficulty (citizen deaths reduce difficulty) |

#### 5.4 Integration with Existing Siege System
- MC raids complement our faction siege system
- Raids are PvE (NPC raiders vs colony)
- Sieges remain PvP (faction vs faction)
- Guard recruits defend against both

---

### PHASE 6: Blocks, Items & Crops
**Priority: MEDIUM — Content**
**Estimated files: ~100**

#### 6.1 Block Registration
Package: `com.ultra.megamod.feature.citizen.block`

**Utility Blocks:**
| Block | Block Entity | Purpose |
|-------|-------------|---------|
| BlockRack | TileEntityRack | Warehouse storage rack |
| BlockStash | TileEntityStash | Personal stash |
| BlockBarrel | TileEntityBarrel | Fluid/item storage |
| BlockScarecrow | TileEntityScarecrow | Farm field marker |
| BlockPlantationField | TileEntityPlantationField | Plantation area |
| BlockDecorationController | TileEntityDecorationController | Blueprint anchor |
| BlockConstructionTape | — | Build zone marker |
| BlockWaypoint | — | NPC navigation point |
| BlockColonySign | TileEntityColonySign | Colony signage |
| BlockCompostedDirt | TileEntityCompostedDirt | Fertilized dirt |
| BlockGrave | TileEntityGrave | Citizen grave |
| BlockNamedGrave | TileEntityNamedGrave | Named citizen grave |
| BlockIronGate | — | Iron gate |
| BlockWoodenGate | — | Wooden gate |
| BlockColonyBanner | TileEntityColonyFlag | Colony banner |
| BlockColonyWallBanner | TileEntityColonyFlag | Wall banner |

**Farming:**
| Block | Purpose |
|-------|---------|
| MinecoloniesFarmland | Standard farmland |
| FloodedFarmland | For rice |
| 15 Crop blocks | Durum, Eggplant, Garlic, Onion, Mint, BellPepper, Tomato, Corn, Cabbage, ButternutSquash, Chickpea, SoyBean, Peas, Rice, NetherPepper |

#### 6.2 Item Registration
Package: `com.ultra.megamod.feature.citizen.item`

**Tools:** ScepterLumberjack, ScepterPermission, ScepterGuard, ScepterBeekeeper, BuildGoggles, ScanAnalyzer, Clipboard
**Scrolls:** ScrollColonyTP, ScrollAreaTP, ScrollBuff, ScrollGuardHelp, ScrollHighlight, ResourceScroll
**Supply:** SupplyChestDeployer, SupplyCampDeployer
**Weapons:** ChiefSword, IronScimitar, PharaoScepter, FireArrow, Spear
**Armor:** 2 Pirate sets (8pc), Plate set (4pc), SantaHat, AssistantHammers (3)
**Sifter Meshes:** String, Flint, Iron, Diamond (4)
**Food:** 95+ items across 3 tiers with biome assignments
**Ingredients:** Doughs (4), Breads (4), Butter, Cornmeal, CreamCheese, SoySauce, RawNoodle
**Bottles:** Water, Milk, SoyMilk, Empty (4)
**Misc:** AncientTome, Compost, Mistletoe, MagicPotion, QuestLog, AdventureToken, ColonyMap, RallyBanner
**Spawn Eggs:** 18 for all raider types

#### 6.3 Creative Tabs
- Add "Colony Huts" tab
- Add "Colony Items" tab  
- Add "Colony Food" tab

---

### PHASE 7: Domum Ornamentum Port
**Priority: MEDIUM — Decorative content, building aesthetics**
**Estimated files: ~80**

#### 7.1 Material Texture System
Package: `com.ultra.megamod.feature.citizen.ornament`

| File | Purpose |
|------|---------|
| `IMateriallyTexturedBlock.java` | Interface for retexturable blocks |
| `IMateriallyTexturedBlockComponent.java` | Component (texture slot) definition |
| `MaterialTextureData.java` | Component → material mapping, NBT serialization |
| `MateriallyTexturedBlockEntity.java` | Stores texture data, syncs to client |
| `MateriallyTexturedBlockManager.java` | Manages all retexturable blocks |

#### 7.2 Architects Cutter
| File | Purpose |
|------|---------|
| `ArchitectsCutterBlock.java` | Crafting station block |
| `ArchitectsCutterBlockEntity.java` | Block entity |
| `ArchitectsCutterMenu.java` | Container with dynamic input slots |
| `ArchitectsCutterScreen.java` | GUI screen |
| `ArchitectsCutterRecipe.java` | Recipe type |
| `ArchitectsCutterRecipeSerializer.java` | Recipe serializer |

#### 7.3 Decorative Blocks (40+ types)
Package: `com.ultra.megamod.feature.citizen.ornament.blocks`

**Frames:** TimberFrameBlock (10 types), FramedLightBlock (6 types), DynamicTimberFrameBlock
**Roof:** ShingleBlock (5 heights), ShingleSlabBlock
**Structure:** PillarBlock (3 shapes), PostBlock, PanelBlock
**Walls:** PaperWallBlock (2 types)
**Doors:** DoorBlock, FancyDoorBlock, FancyTrapdoorBlock
**Decorative:** ExtraBlock (27 types), BarrelBlock (2 variants), FloatingCarpetBlock (16 colors)
**Bricks:** AllBrickBlock (2), AllBrickStairBlock (2), BrickBlock (10 types)
**Vanilla-compat:** FenceBlock, FenceGateBlock, SlabBlock, WallBlock, StairBlock, TrapdoorBlock

#### 7.4 Client Rendering
Package: `com.ultra.megamod.feature.citizen.ornament.client`

| File | Purpose |
|------|---------|
| `MateriallyTexturedModelLoader.java` | IGeometryLoader for custom model loading |
| `MateriallyTexturedGeometry.java` | Geometry baking with parent model |
| `MateriallyTexturedBakedModel.java` | Dynamic model caching (retextured quads, 2min TTL) |
| `RetexturedBakedModelBuilder.java` | Map components → replacement blocks |
| `ModelSpriteQuadTransformer.java` | UV/sprite replacement in vertex data |

#### 7.5 Material Tags
18 tags defining which blocks can be used for each component type.

---

### PHASE 8: Multi-Piston
**Priority: LOW — Single feature block**
**Estimated files: ~5**

Package: `com.ultra.megamod.feature.citizen.multipiston`

| File | Purpose |
|------|---------|
| `MultiPistonBlock.java` | Block with redstone detection |
| `MultiPistonBlockEntity.java` | Direction, range, speed, tick-based movement |
| `MultiPistonScreen.java` | GUI (direction dropdowns, range/speed inputs) |
| `MultiPistonPayload.java` | Client→Server config sync |

---

### PHASE 9: UI Integration
**Priority: HIGH — Player-facing**
**Estimated files: ~40**

#### 9.1 Computer TownScreen Enhancement
Enhance existing 9-tab TownScreen with MineColonies features:

| Tab | Enhancement |
|-----|------------|
| Overview | Add citizen count, happiness avg, research progress, building count, visitor count |
| Workers | ENHANCED — Show all 44 job types, skill display (11 skills), food/happiness/disease status |
| Recruits | Keep existing + add Guard Tower integration, patrol config |
| Groups | Keep existing |
| Diplomacy | Keep existing |
| Territory | ENHANCED — Show waypoints, construction tape, colony borders |
| Upkeep | ENHANCED — Request system pending requests, minimum stock alerts |
| War | ENHANCED — Raid history, difficulty display, 5 raider cultures |
| Work Orders | ENHANCED — Blueprint-based orders, builder assignment, material lists |
| **Research** (NEW) | Research tree browser, branch selection, cost/requirement display |
| **Buildings** (NEW) | Building browser, level display, upgrade buttons, module config |
| **Requests** (NEW) | Active requests, resolver status, manual fulfillment |

#### 9.2 Building Interaction Screens
When player right-clicks a hut block → opens building-specific screen:

| Screen | Purpose |
|--------|---------|
| `BuildingMainScreen.java` | Generic building info (level, workers, upgrade, repair) |
| `CraftingBuildingScreen.java` | Recipe management for crafting buildings |
| `FarmerBuildingScreen.java` | Field assignment, fertilize toggle |
| `MinerBuildingScreen.java` | Level depth, fill block selection |
| `WarehouseBuildingScreen.java` | Storage view, sort, courier config |
| `BarracksBuildingScreen.java` | Guard management, patrol config |
| `SchoolBuildingScreen.java` | Pupil/teacher assignment |
| `UniversityBuildingScreen.java` | Research queue |
| `HospitalBuildingScreen.java` | Disease treatment status |
| `TavernBuildingScreen.java` | Visitor management |

#### 9.3 Citizen Interaction Enhancement
Enhance existing `CitizenInteractionScreen`:
- Show 11 skills with levels
- Happiness breakdown
- Disease status
- Food/saturation
- Active requests
- Job assignment
- Home assignment

#### 9.4 Admin Panel Integration
Add to existing Admin Panel tabs:
- **Colony Admin** — View all colonies, force raids, modify research, spawn citizens
- **Building Admin** — Instant build, level override, module editing
- **Raider Admin** — Trigger specific raid types, set difficulty

---

### PHASE 10: World Generation & Data
**Priority: LOW — Content richness**
**Estimated files: ~30**

#### 10.1 Colony World Generation
| File | Purpose |
|------|---------|
| `ColonyStructure.java` | Jigsaw structure definition for world-gen colonies |
| `ColonyStructureProcessor.java` | Block replacement processors per style |
| `RaiderCampStructure.java` | Raider camp spawning (5 culture variants) |
| `SupplyShipStructure.java` | Supply ship ocean spawning |

#### 10.2 Style Packs
Port all 22 building styles as blueprint packs:
Acacia, Ancient Athens, Birch, Caledonia, Cavern, Colonial, Dark Oak, Fortress, Incan, Jungle, Lost City, Medieval Birch/Oak/Spruce/Dark Oak, Nordic, Original, Pagoda, Sandstone, Shire, True Dwarven, Warped Nether

#### 10.3 Data Generation
| File | Purpose |
|------|---------|
| `ColonyBlockTagsProvider.java` | Block tags |
| `ColonyItemTagsProvider.java` | Item tags |
| `ColonyRecipeProvider.java` | Crafting recipes |
| `ColonyLootTableProvider.java` | Block/entity loot |
| `ColonyAdvancementProvider.java` | Advancements |
| Worker-specific crafting providers (18) | Per-job recipes |

#### 10.4 Custom Citizen Names
Data-driven name lists per culture:
- `data/megamod/citizennames/*.json`

#### 10.5 Disease Data
- `data/megamod/colony/diseases/*.json`

---

### PHASE 11: Commands & Enchantments
**Priority: LOW — Polish**
**Estimated files: ~20**

#### 11.1 Commands
Add `/colony` command tree:
- `/colony info`, `/colony list`, `/colony teleport`, `/colony claim`
- `/citizen info`, `/citizen list`, `/citizen modify`, `/citizen spawn`
- `/colony raid <type>`, `/colony backup`, `/colony delete`
- `/kill raiders`, `/kill animals`

#### 11.2 Enchantments
| Enchantment | Effect |
|-------------|--------|
| Raider Damage | Bonus damage to raider entities |

---

## Integration Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        MEGAMOD CORE                             │
│  MegaMod.java ──── MegaModClient.java ──── Config.java         │
└─────────────┬───────────────────────────────────┬───────────────┘
              │                                   │
   ┌──────────▼──────────┐             ┌──────────▼──────────┐
   │   EXISTING SYSTEMS  │             │   COLONY SYSTEM     │
   │                     │             │   (ENHANCED)        │
   │ Dungeons, Casino,   │             │                     │
   │ Backpacks, Alchemy, │             │ ┌─────────────────┐ │
   │ Relics, Museum,     │◄────────────┤ │ FactionManager  │ │
   │ HUD, Admin Modules, │  Economy    │ │ (= ColonyMgr)   │ │
   │ Bot Control, Skills,│  Integration│ ├─────────────────┤ │
   │ Furniture, Arena,   │             │ │ BuildingRegistry│ │
   │ Corruption, Map,    │             │ │ + Module System │ │
   │ Vanilla Refresh     │             │ ├─────────────────┤ │
   │                     │             │ │ RequestManager  │ │
   └─────────────────────┘             │ ├─────────────────┤ │
                                       │ │ ResearchManager │ │
   ┌─────────────────────┐             │ ├─────────────────┤ │
   │   COMPUTER SYSTEM   │             │ │ RaidManager     │ │
   │                     │             │ │ (5 cultures)    │ │
   │ TownScreen (12 tabs)│◄────────────┤ ├─────────────────┤ │
   │ BuildingScreens     │             │ │ 44 Job Types    │ │
   │ ResearchScreen      │             │ │ 46 Buildings    │ │
   │ RequestScreen       │             │ │ 11 Skills       │ │
   │ Admin Colony Panel  │             │ └─────────────────┘ │
   └─────────────────────┘             └──────────┬──────────┘
                                                  │
              ┌───────────────────────────────────┤
              │                                   │
   ┌──────────▼──────────┐             ┌──────────▼──────────┐
   │  BLUEPRINT SYSTEM   │             │  DOMUM ORNAMENTUM   │
   │  (Structurize Port) │             │                     │
   │                     │             │ Architects Cutter   │
   │ .blueprint format   │             │ 40+ block types     │
   │ Scan/Build/Shape    │             │ Material textures   │
   │ Tag tools           │             │ Dynamic models      │
   │ 22 style packs      │             │                     │
   │ Ghost rendering     │             ├─────────────────────┤
   │ Placement handlers  │             │  MULTI-PISTON       │
   │ Pack browser        │             │                     │
   └─────────────────────┘             │ Configurable block  │
                                       │ push/pull           │
                                       └─────────────────────┘
```

---

## Mapping: MineColonies → MegaMod Adaptation

| MineColonies Concept | MegaMod Equivalent | Adaptation Notes |
|---------------------|-------------------|-----------------|
| Colony | Faction | FactionData enhanced with building/research/request managers |
| Colony Owner | Faction Leader | Same concept |
| Colony Permissions | Existing PlayerRank | Enhance with MC's action-based permissions |
| Town Hall | Faction HQ / TownScreen | Computer app remains primary interface |
| Colony Border | Chunk Claims | Already have this |
| Citizen Entity | AbstractCitizenEntity | Enhance with 11 skills, disease, happiness depth |
| Citizen Data | Keep existing + extend | Add handlers for food, disease, mourn |
| Jobs | CitizenJob enum | Expand from 24 to 55+ entries |
| Building Modules | NEW | Add module composition system |
| Request System | NEW | Entirely new — critical for MC gameplay |
| Research | NEW | University-driven progression |
| Raids (PvE) | Enhance ColonyRaidManager | Add 5 cultures, difficulty scaling |
| Sieges (PvP) | Keep existing | Our unique feature |
| Guards | Merge with Recruits | MC guards + our recruits coexist |
| Visitors | NEW | Tavern visitors, recruitment |
| BlockUI | NOT ported | Use our Screen-based UI |
| Blueprint format | NEW alongside .schem | Support both formats |
| Schematic Packs | Blueprint packs | 22 styles in /blueprints/ |
| DO blocks | NEW | Architects Cutter + all decorative types |
| Multi-Piston | NEW | Single block addition |
| Economy | Keep MegaCoins | MC has no economy; ours is unique |
| Groups/Formations | Keep existing | Our unique military feature |
| Diplomacy | Keep existing | Our faction diplomacy stays |

---

## Implementation Order Summary

| Phase | Name | Files | Dependencies |
|-------|------|-------|-------------|
| 0 | Structurize Blueprint System | ~60 | None — foundation |
| 1 | Building Module System & Core Buildings | ~120 | Phase 0 |
| 2 | Request/Delivery System | ~40 | Phase 1 |
| 3 | Worker Expansion (31 new jobs) | ~90 | Phase 1, 2 |
| 4 | Research System | ~25 | Phase 1 |
| 5 | Raider System (5 cultures) | ~50 | Phase 1 |
| 6 | Blocks, Items & Crops | ~100 | Phase 1 |
| 7 | Domum Ornamentum | ~80 | None (parallel) |
| 8 | Multi-Piston | ~5 | None (parallel) |
| 9 | UI Integration | ~40 | Phase 1-6 |
| 10 | World Gen & Data | ~30 | Phase 6 |
| 11 | Commands & Enchantments | ~20 | Phase 1 |
| **TOTAL** | | **~660 files** | |

---

## Parallelizable Work

These phases can run in parallel:
- **Phase 7 (Domum Ornamentum)** — independent of colony system
- **Phase 8 (Multi-Piston)** — independent standalone block
- **Phase 4 (Research)** and **Phase 5 (Raiders)** — both depend on Phase 1 but not each other
- **Phase 6 (Blocks/Items)** — registration can happen alongside Phase 3

---

## Asset Requirements

### Textures Needed
- 54 hut block textures (16x16)
- 15 crop growth stage textures (per stage × 15 crops)
- 18 raider entity textures + models
- 40+ Domum Ornamentum block textures
- Multi-Piston block texture
- 95+ food item textures
- Tool/weapon/armor textures
- All GUI textures for new screens

### Models Needed
- Raider entity models (Barbarian, Pirate, Mummy, Norsemen, Amazon variants)
- Decorative block models (Domum Ornamentum JSON models)
- Hut block models
- Gate models (iron/wooden)

### Sounds Needed
- Raider ambient/attack/death sounds (per culture)
- Building construction sounds
- Citizen work sounds (per job)
- Research complete sound

### Language Files
- All new blocks/items/entities need en_us.json entries
- Building names, job names, skill names, research names
- GUI text for all new screens

---

## Data Files Needed

```
data/megamod/
├── citizennames/           (Name lists per culture)
├── colony/
│   ├── diseases/           (Disease definitions)
│   └── research/           (Research tree JSON)
├── crafterrecipes/         (Per-job recipe lists)
├── tags/
│   ├── blocks/             (Block tags for DO components)
│   └── items/              (Item tags)
├── loot_tables/
│   ├── blocks/             (Block drops)
│   └── entities/           (Raider drops)
├── recipes/                (Crafting recipes)
├── advancements/           (Colony advancements)
└── worldgen/
    ├── structure/          (Colony/camp definitions)
    ├── structure_set/      (Spawning rules)
    ├── template_pool/      (Jigsaw pools)
    └── processor_list/     (Block replacement)

blueprints/megamod/
├── acacia/                 (Acacia style pack)
├── medieval_oak/           (Medieval Oak style)
├── colonial/               (Colonial style)
└── ... (22 styles total, each with buildings at levels 1-5)
```

---

## Risk Factors & Mitigations

| Risk | Impact | Mitigation |
|------|--------|-----------|
| Blueprint format compatibility | MC schematics won't work | Dual format support (.blueprint + .schem) |
| Performance with many buildings | Tick lag | Rate-limited ticking, chunk-based loading |
| Request system complexity | Bugs, edge cases | Thorough testing, simplified resolver chain |
| Model rendering (DO) | Client performance | Cache with TTL, limit retexture operations |
| Raider entity registration | ID conflicts | Dedicated entity type namespace |
| 660+ new files | Maintenance burden | Clear package organization, shared base classes |
| Existing system conflicts | Breaking changes | Incremental integration, feature flags |

---

## Notes

- **BlockUI is NOT ported** — MineColonies uses BlockUI for all GUIs. We use Minecraft's native Screen system via our Computer/Phone interface. Every MC window must be reimplemented as a MegaMod Screen.
- **JEI integration** — MC has extensive JEI compat for recipe display. Add JEI plugin if JEI is present (soft dependency).
- **Structurize's pack system replaces our /blueprints/ directory** — but we keep backward compat with existing .schem files.
- **Building styles** — The 22 style packs are primarily .blueprint files and pack.json metadata. These are data, not code.
- **Citizen textures** — MC uses a model type registry for different citizen appearances. We already have CitizenModel/CitizenBiomeLayer — enhance with MC's variety.

---

## SECOND PASS ADDITIONS (Verified Systems & Missing Elements)

### Schematic Packs: CORRECTED — 26 Packs, 9,534 Blueprints

The original plan listed 22 styles. The actual count is **26 packs** (including 1 empty template):

| # | Pack Name | Display Name | Blueprints | Author(s) |
|---|-----------|-------------|------------|-----------|
| 1 | acacia | Urban Savanna | 401 | John Reiher |
| 2 | ancientathens | Ancient Athens | 528 | John Reiher, Pixblockus, Raycoms, Thodor12 |
| 3 | birch | Urban Birch | 326 | Raycoms, Aravan, Cugo |
| 4 | **caledonia** | **Caledonia** | **432** | **Thaylar** |
| 5 | cavern | [Advanced] Cavern Style | 719 | (community) |
| 6 | colonial | [Beginner] Colonial | 510 | (community) |
| 7 | darkoak | Dark Oak Treehouse | 291 | Annie, Cugo |
| 8 | fortress | Fortress | 461 | Aravan |
| 9 | incan | Incan | 331 | Raycoms, John Reiher, SomeAddons |
| 10 | jungle | Jungle Treehouse | 410 | John Reiher |
| 11 | lostcity | Lost Mesa City | 395 | Raycoms, John Reiher, SomeAddons |
| 12 | medievalbirch | Medieval Birch | 395 | Carlansor |
| 13 | medievaldarkoak | Medieval Dark Oak | 394 | Carlansor |
| 14 | medievaloak | Medieval Oak | 440 | Carlansor |
| 15 | medievalspruce | Medieval Spruce | 405 | Carlansor |
| 16 | nordic | Nordic Spruce | 406 | Luna, John Reiher |
| 17 | original | Minecolonies Original | 550 | many |
| 18 | pagoda | Pagoda | 455 | Thaylar |
| 19 | sandstone | Desert Oasis | 384 | Raycoms, John Reiher, SomeAddons |
| 20 | shire | Shire | 395 | (community) |
| 21 | **spacewars** | **Space Wars** | **318** | **JohnReiher & Raycoms** |
| 22 | truedwarven | Stalactite Caves | 235 | Luna |
| 23 | warped | Warped Netherlands | 264 | Cugo |
| 24 | template | Template | 0 | (empty template for custom packs) |

**Caledonia Pack — Full Structure Verified:**
432 blueprints organized into: agriculture/ (60), craftsmanship/ (105), decorations/ (32), education/ (15), fundamentals/ (45), infrastructure/ (84), military/ (30), mystic/ (20), walls/ (36). All buildings at levels 1-5.

**Standard Pack Directory Structure:**
```
[style]/
├── pack.json
├── agriculture/ (horticulture/, husbandry/)
├── craftsmanship/ (carpentry/, luxury/, masonry/, metallurgy/, storage/)
├── decorations/ (arches/, decorative/, housing/, planning/, plaza/, supplies/, utility/)
├── education/ (library, school, university 1-5)
├── fundamentals/ (builder, cook, hospital, lumberjack, miner, residence, tavern, townhall 1-5)
├── infrastructure/ (alleys/, avenues/, birail/, fields/, mineshafts/, monorail/, plaza/, roads/)
├── military/ (altguardtower, archery, barracks, barrackstower, combatacademy, gatehouse, guardtower 1-5)
├── mystic/ (enchanter, graveyard, mysticalsite, netherworker 1-5)
└��─ walls/ (corners/, gates/, misc/, stairs/, walls/)
```

**Secondary structures in data/minecolonies/structures/:** 326 NBT files across 14 styles (for world gen colonies).

---

### GUI Screens: 215 Total Components to Reimplement

All MineColonies GUIs use BlockUI XML. We must reimplement each as MegaMod Screens.

**106 Java Window classes:**
- 5 Abstract base classes
- 33 Root windows (building browser, clipboard, hire worker, research tree, etc.)
- 7 Citizen windows (main info, family, happiness, job, requests)
- 8 Container/inventory windows (crafting, furnace, grave, rack, field)
- 4 Hut-specific windows (barracks, builder module, living, worker placeholder)
- 5 Map/colony view windows (colony map, prestige ranking, zoom drag)
- 36 Module windows (crafting settings, farm fields, graveyard, minimum stock, plantation, restaurant menu, university, warehouse, work orders, etc.)
- 6 Quest log windows (available, in-progress, finished quests)
- 14 Town hall windows (main, citizens, permissions, settings, stats, alliance, mercenary, name entry, colony management)

**96 XML layout files** (reference for reimplementation):
- 2 Analyzer layouts
- 10 Citizen layouts
- 14 Town hall layouts
- 5 Map layouts
- 38 Hut setting layouts (the building module UIs)
- 27 Main window layouts

**12 Structurize XML layouts** (scan tool, build tool, shape tool, tag tool, undo/redo, pack switcher)

**1 Domum Ornamentum screen** (Architects Cutter — already a ContainerScreen, easy port)

---

### Crafting & Recipe System: CRITICAL NEW PHASE

**This was significantly underrepresented in the original plan and needs its own phase.**

#### Custom Recipe System Architecture:

**Recipe Data Format (JSON in datapacks):**
```json
{
  "type": "recipe|recipe-multi-out|recipe-template|remove",
  "crafter": "blacksmith_custom",
  "inputs": [{"item": "minecraft:iron_ore", "count": 2}],
  "result": {"item": "minecraft:iron_ingot"},
  "alternate-output": [...],
  "additional-output": [...],
  "tool": "minecolonies:axe",
  "intermediate": "minecraft:furnace",
  "research-id": "minecolonies:technology/hot_iron",
  "not-research-id": "...",
  "min-building-level": 3,
  "max-building-level": 5,
  "loot-table": "minecolonies:fisherman",
  "must-exist": true,
  "show-tooltip": true,
  "tag": "minecraft:logs",
  "filter": {"include": ["_log"], "exclude": ["stripped_"]}
}
```

**Recipe Template System:**
- Templates generate recipes from Minecraft item tags automatically
- Example: `tag: "minecraft:logs"` + filter → generates stripped log recipes for ALL log types
- Uses `[NS]` (namespace) and `[PATH]` (item path) substitution

**Recipe Priority & Selection:**
- Recipes stored as ordered list per building
- Two modes: Priority (first valid wins) or Max-Stock (use most abundant ingredient)
- Workers can toggle recipes on/off
- Player reorders via GUI

**Recipe Improvement System:**
- Workers optimize recipes over time
- Chance: `min(5%, 0.0625 × craftCount + 0.0625 × skill)`
- Reduces ingredient count by 1 on `CRAFTING_REDUCEABLE` tagged inputs
- Creates new optimized recipe variant

**Recipe Execution Pipeline:**
```
GET_RECIPE → QUERY_ITEMS → CRAFT (100 ticks progress) → Output
```

**Custom Ingredient Types:**
- `FoodIngredient`: Match by nutrition/saturation range
- `PlantIngredient`: Match by plant type/growth

**Composting System:**
- Strength-based: items needed = 64 / strength
- 24000 tick fermentation (20 minutes)
- Output: 6× Compost

---

### XP & Skill System: EXACT SPECIFICATIONS

**Citizen XP Formula:**
```
localXp = baseXp × (1 + (workBuildingLevel + citizenHutLevel) / 10)
localXp += localXp × (intelligenceLevel / 100.0)
If saturation <= 0: NO XP gained
Multiplied by LEVELING research effect
```

**Skill Distribution Per Work Action:**
- Primary skill: 100% XP
- Primary's Complimentary skill: +10% XP
- Primary's Adverse skill: -10% XP
- Secondary skill: 50% XP
- Secondary's Complimentary: +5% XP
- Secondary's Adverse: -5% XP

**Level Cap:** `min(citizenHutMaxLevel × 10, 255)`

**All Job → Skill Assignments (Verified from Source):**

| Job | Primary | Secondary |
|-----|---------|-----------|
| **Horticulture** | | |
| Farmer | Stamina | Athletics |
| Composter | Stamina | Athletics |
| Florist | Dexterity | Agility |
| Beekeeper | Dexterity | Adaptability |
| Planter | Agility | Dexterity |
| **Animal Husbandry** | | |
| Chicken Herder | Adaptability | Agility |
| Cowboy | Athletics | Stamina |
| Shepherd | Focus | Strength |
| Swine Herder | Strength | Athletics |
| Rabbit Herder | Agility | Athletics |
| Fisherman | Focus | Agility |
| **Crafting** | | |
| Alchemist | Dexterity | Mana |
| Baker | Knowledge | Dexterity |
| Blacksmith | Strength | Focus |
| Chef | Creativity | Knowledge |
| Concrete Mixer | Stamina | Dexterity |
| Cook | Adaptability | Knowledge |
| Crusher | Stamina | Strength |
| Dyer | Creativity | Dexterity |
| Fletcher | Dexterity | Creativity |
| Glassblower | Creativity | Focus |
| Mechanic | Knowledge | Agility |
| Sawmill | Knowledge | Dexterity |
| Sifter | Focus | Strength |
| Smelter | Athletics | Strength |
| Stone Smelter | Athletics | Dexterity |
| Stonemason | Creativity | Dexterity |
| Lumberjack | Strength | Focus |
| **Service** | | |
| Builder | Adaptability | Athletics |
| Deliveryman | Agility | Adaptability |
| Enchanter | Mana | Knowledge |
| Healer | Mana | Knowledge |
| Undertaker | Strength | Mana |
| **Education** | | |
| Pupil | Knowledge | Mana |
| Teacher | Knowledge | Mana |
| Student | Intelligence | Intelligence |
| Researcher | Knowledge | Mana |
| **Mining** | | |
| Miner | Strength | Stamina |
| Quarrier | Strength | Stamina |
| Nether Worker | Adaptability | Strength |
| **Military Training** | | |
| Archer Training | Agility | Adaptability |
| Knight Training | Adaptability | Stamina |

---

### Happiness System: EXACT WEIGHTS & FORMULA

**Static Modifiers:**
| Modifier | Weight | Calculation |
|----------|--------|-------------|
| School | 1.0 | Requires school level ≥ 1 |
| Mystical Site | 1.0 | max(1, mysticalSiteMaxLevel / 2.0) |
| Security | 4.0 | min(guards / (workers × 2/3), 2) |
| Social | 2.0 | (Total - Unemployed - Homeless - Sick - Hungry) / Total |
| Food | 3.0 | avg(diversity: uniqueFoods/homeLevel, quality: mcFoods/max(1, homeLevel-2)) capped at 5 each |

**Time-Based Modifiers (Degrade):**
| Modifier | Weight | Stages |
|----------|--------|--------|
| Homelessness | 3.0 | 0.75 after COMPLAIN days, 0.5 after DEMANDS days |
| Unemployment | 2.0 | 0.75 after COMPLAIN days, 0.5 after DEMANDS days |
| Health/Disease | 2.0 | 0.5 after COMPLAIN days, 0.1 after DEMANDS days |
| Idle at Job | 1.0 | 0.5 after COMPLAIN days, 0.1 after DEMANDS days |
| Sleep | 1.5 | Progressive: (0→2d), (2→1.6d), (3+→1d) |

**Formula:**
```
totalHappiness = sum(factor × weight) / totalWeight
finalHappiness = totalHappiness × (1 + researchLeveling) × 10
Capped at 10.0
```

---

### Disease System: COMPLETE MECHANICS

- **Base chance:** 1 in (diseaseModifier × DISEASE_FACTOR / 100000) per tick
- **Spread:** 1% chance on collision with sick citizen
- **Masks research:** Reduces transmission up to 100%
- **Immunity after cure:** 90 minutes (900 min with vaccine research)
- **Half immunity** if not cured in hospital bed
- **Healers cannot get sick**
- **Food diversity reduces susceptibility:** `baseModifier × 0.5 × min(2.5, 5.0/diversity)`

---

### Visitor & Recruitment System

- Visitors spawn at **Tavern** building
- Each visitor has an **ItemStack cost** to recruit (7 tiers from dried kelp to steak dinner)
- Recruitment tiers: T1 (bread, apple), T2 (leather, cooked fish), T3 (cooked meat), T4 (iron, gold, redstone), T5 (honey, MineColonies food), T6 (blaze powder, spider eye), T7 (premium MineColonies food)
- Unrecruited visitors leave (VISITORS_ABSCONDED stat)
- Recruited visitors become citizens (VISITORS_RECRUITED stat)

---

### Children & Education System

- Citizens can have children via breeding mechanics
- Children flagged with `isChild()` in CitizenData
- **School building** employs Pupils (children only, max 2× school level)
- **Teachers** educate Pupils, boosting Intelligence stat
- Children grow to adults at age threshold
- Adults available for regular jobs

---

### Colony Statistics Tracking (30+ Stats)

| Stat | Description |
|------|-------------|
| TREE_CUT | Trees felled |
| DEATH | Citizen deaths |
| BIRTH | Citizens born |
| ORES_MINED | Ores extracted |
| BLOCKS_MINED | Blocks broken |
| BLOCKS_PLACED | Blocks placed |
| MOBS_KILLED | Hostile mobs killed |
| ITEMS_DELIVERED | Deliveryman deliveries |
| ITEMS_CRAFTED | Items crafted by workers |
| FOOD_SERVED | Meals served by cook |
| CITIZENS_HEALED | Hospital treatments |
| CROPS_HARVESTED | Farm harvests |
| LAND_TILLED | Farmland created |
| FISH_CAUGHT | Fish caught |
| BUILD_BUILT/UPGRADED/REPAIRED/REMOVED | Construction events |
| ITEMS_COOKED | Food cooked |
| NEW_VISITORS | Tavern visitors |
| VISITORS_RECRUITED | Visitors recruited |
| VISITORS_ABSCONDED | Visitors who left |
| ARROWS_FIRED/HIT | Archery accuracy |
| LESSONS_GIVEN | School lessons |

Per-day tracking with period queries. Synced to client for UI display.

---

### Colony Advancements (20+)

**Progression Track:**
1. Place supply chest → 2. Place Town Hall → 3. Create build request → 4. Fulfill request → 5. Build Builder Hut (L1) → 6. Build Builder Hut (L2) → 7. Build Guard Tower → 8. Build Mystical Site → 9. Build Mystical Site (L5) → 10. Build Tavern → 11. Build Citizen Home

**Milestone Triggers:**
- Population milestones (5, 10, 20 citizens)
- Food variety achievements
- All towers built
- Deep mining
- Army population
- Recipe additions

**Custom Advancement Triggers:**
PlaceStructureCriterion, CompleteBuildRequestCriterion, ColonyPopulationCriterion, CitizenEatFoodCriterion, CreateBuildRequestCriterion, AllTowersCriterion, DeepMineCriterion

---

### Quest System

- Quest instances with giver (citizen), objectives, participants, rewards
- Template-based objectives via `IQuestObjectiveTemplate`
- Delivery objectives supported
- Dialogue-based with answers and final answers
- Progress tracking with `advanceObjective()`
- Expiration based on colony day count
- Data-driven from JSON datapacks

---

### Colony Connection/Diplomacy Events

| Event Type | Purpose |
|-----------|---------|
| ALLY_REQUEST | Request alliance |
| ALLY_CONFIRMED | Alliance accepted |
| FEUD_STARTED | War declared |
| NEUTRAL_SET | Return to neutral |
| DISCONNECTED | Connection lost |

Managed by `IColonyConnectionManager`, displayed in Alliance page.

---

### Stories/Lore System

- Abandoned colony names (biome-specific: wet, cold, desert, etc.)
- Supply camp/ship stories (lore text)
- Data-driven from JSON in `data/minecolonies/colony/stories/`
- Displayed when placing supply items for first time
- Can be marked as read via `MarkStoryReadOnItemMessage`

---

### Lucky Ore System (Miner Bonus)

5 progressive tiers matching miner level:

| Level | Ores (weight decreasing) |
|-------|--------------------------|
| 1 | Coal (64), Copper (48) |
| 2 | + Iron (32), Gold (16) |
| 3 | + Redstone (8), Lapis (4) |
| 4 | + Diamond (2), Emerald (1) |
| 5 | Same as Level 4 |

---

### Waypoint System

- `BlockWaypoint` — zero hardness, non-solid navigation block
- Placed in schematics to guide citizen pathfinding
- Citizens path toward nearest waypoint when navigating
- Used heavily in infrastructure schematics (roads, alleys)

---

### Colony Protection Details

**Explosion protection levels:**
1. DAMAGE_NOTHING — full protection
2. DAMAGE_PLAYERS — only damage players
3. DAMAGE_ENTITIES — damage entities but not blocks
4. DAMAGE_EVERYTHING — no protection

TNT, creeper, fire protection via permission event handler. Block place/break via action-based permission checks per rank.

---

### Supply Camp & Ship — Colony Founding

**Flow:**
1. Player crafts Supply Camp or Supply Ship item
2. Right-click to place → shows story window (first time)
3. Opens WindowSupplies for placement (blueprint from selected style)
4. Placement validation: 25% tolerance for failed block checks
5. Creates initial colony structure from blueprint
6. Colony automatically founded at location

---

### Animal Management System

- `IAnimalManager` with `IAnimalData` tracking
- Animals tracked by ID with UUID mapping
- Colony-level animal registration
- Staggered tick updates for performance
- NBT persistence and network synchronization
- Client view data via `ColonyViewAnimalViewDataMessage`

---

### Wiki Items NOT in Source Code

| Item | Status |
|------|--------|
| Calipers | NOT FOUND — wiki-only or not yet implemented |
| Tag Anchor Block | NOT FOUND — wiki-only or different version |
| Carpenter (worker) | ALIAS — actual job is Sawmill worker |
| Library Student | ALIAS — actual job is Pupil |
| Apiary | ALIAS — actual building is Beekeeper |

---

### REVISED FILE COUNT

| Phase | Original | Added | New Total |
|-------|----------|-------|-----------|
| 0 | ~60 | +5 (recipe system integration) | ~65 |
| 1 | ~120 | +15 (crafting modules) | ~135 |
| 2 | ~40 | +10 (recipe resolvers) | ~50 |
| 3 | ~90 | +20 (crafting AI, recipe data) | ~110 |
| 4 | ~25 | — | ~25 |
| 5 | ~50 | +5 (camp structures) | ~55 |
| 6 | ~100 | +10 (compost, lucky ore) | ~110 |
| 7 | ~80 | — | ~80 |
| 8 | ~5 | — | ~5 |
| 9 | ~40 | +30 (215 GUI reimplementations) | ~70 |
| 10 | ~30 | +15 (stories, quests, advancements, names) | ~45 |
| 11 | ~20 | +5 (statistics, animal mgr) | ~25 |
| **TOTAL** | **~660** | **+115** | **~775 files** |
