# MineColonies Integration — Remaining Work Plan

## Status: Architecture Complete, Implementation ~35% Functional

This document lists EVERY remaining task needed to make the MineColonies port fully functional — matching the original in gameplay, not just data structures.

---

## PRIORITY 1: Server-Side Building Action Handlers (Makes existing screens work)

### TownHandler.java — Missing Action Handlers (~50 actions)

Every building screen sends `ComputerActionPayload` with an action string. These handlers need to be added to `TownHandler.java`:

**Building Management:**
- [ ] `building_upgrade` — Create ColonyWorkOrder, validate level, deduct materials ✓ (partially done)
- [ ] `building_repair` — Create repair work order ✓ (partially done)
- [ ] `building_rename` — Set custom name on TileEntityColonyBuilding ✓ (done)
- [ ] `building_demolish` — Remove building, free workers, refund partial materials
- [ ] `building_assign_worker` — Assign citizen to WorkerBuildingModule slot
- [ ] `building_remove_worker` — Remove citizen from building
- [ ] `building_set_style` — Change building style/pack
- [ ] `building_reactivate` — Reactivate a deactivated building

**Crafting Building Actions:**
- [ ] `building_add_recipe` — Add recipe to ICraftingBuildingModule
- [ ] `building_remove_recipe` — Remove recipe
- [ ] `building_recipe_priority_up` — Move recipe up in priority
- [ ] `building_recipe_priority_down` — Move recipe down
- [ ] `building_toggle_recipe` — Enable/disable recipe

**Farmer Actions:**
- [ ] `building_assign_field` — Link scarecrow field to farmer building
- [ ] `building_remove_field` — Unlink field
- [ ] `building_toggle_fertilize` — Toggle fertilizer mode

**Miner Actions:**
- [ ] `building_set_mine_level` — Set mining depth tier
- [ ] `building_set_fill_block` — Set fill block type
- [ ] `building_repair_mine_level` — Repair a mine level

**Guard Actions:**
- [ ] `building_set_patrol_mode` — Set patrol/follow/guard mode
- [ ] `building_add_patrol_point` — Add patrol waypoint
- [ ] `building_remove_patrol_point` — Remove waypoint
- [ ] `building_set_guard_target` — Set guard follow target

**Warehouse Actions:**
- [ ] `building_sort_warehouse` — Sort all rack inventories
- [ ] `building_assign_courier` — Assign deliveryman to warehouse
- [ ] `building_set_min_stock` — Set minimum stock level for item

**University Actions:**
- [ ] `building_cancel_research` — Cancel active research
- [ ] `building_set_research_queue` — Set next research

**Tavern Actions:**
- [ ] `building_recruit_visitor` — Recruit a visitor (check cost, deduct items)

**Hospital Actions:**
- [ ] `building_assign_bed` — Assign sick citizen to hospital bed

**Misc Actions:**
- [ ] `building_set_hiring_mode` — Auto/manual hiring
- [ ] `building_toggle_auto_hire` — Toggle auto-hire for building

---

## PRIORITY 2: Item Functionality (10+ items need real logic)

### Items That Need `use()` Overrides

- [ ] **Scroll: Colony TP** (`scroll_tp`) — Teleport player to their colony center (town chest position)
- [ ] **Scroll: Area TP** (`scroll_area_tp`) — Teleport to a claimed chunk
- [ ] **Scroll: Buff** (`scroll_buff`) — Apply speed/resistance buff for 60 seconds
- [ ] **Scroll: Guard Help** (`scroll_guard_help`) — Summon nearest guards to player position
- [ ] **Scroll: Highlight** (`scroll_highlight`) — Highlight nearby colony buildings with glowing effect
- [ ] **Resource Scroll** (`resource_scroll`) — Show builder's required materials list
- [ ] **Clipboard** (`clipboard`) — Right-click to show colony work orders and pending requests in chat
- [ ] **Rally Banner** (`banner_rally_guards`) — Right-click to rally all guards to clicked position
- [ ] **Colony Map** (`colony_map`) — Right-click to open map screen showing colony layout
- [ ] **Quest Log** (`quest_log`) — Shift+right-click on hut block to show active quests
- [ ] **Fire Arrow** (`fire_arrow`) — Arrow that sets targets on fire (extend AbstractArrow)
- [ ] **Magic Potion** (`magic_potion`) — Drinkable potion with random positive effect
- [ ] **Ancient Tome** (`ancient_tome`) — Right-click to show lore text

---

## PRIORITY 3: Citizen AI ↔ Building Module Wiring

### The Problem
Worker AIs (FarmerAI, BakerAI, etc.) exist as Goal classes but operate independently of the building module system. They need to:
1. Check their assigned building's WorkerBuildingModule for job type
2. Use the building's position as their work site
3. Deposit outputs into the building's storage
4. Request materials from the request system
5. Gain XP through the CitizenSkillData system

### Tasks
- [ ] Create `BuildingWorkSite` interface — connects AI to building position + storage
- [ ] Wire each worker AI to query their building's inventory for materials
- [ ] Wire each worker AI to deposit products into building inventory
- [ ] Wire crafting AIs to use ICraftingBuildingModule recipe list
- [ ] Wire FarmerAI to use building's assigned fields (scarecrow positions)
- [ ] Wire MinerAI to use building's depth tier setting
- [ ] Wire GuardAI to use building's patrol mode + patrol points
- [ ] Wire DeliverymanAI to query RequestManager for pending deliveries
- [ ] Wire ResearcherAI to call ResearchManager.tickResearch() (done)

---

## PRIORITY 4: Production Chain Simulation

### The Core Loop
Every production building needs this cycle:
```
Building has recipe → Worker checks recipe → Worker gathers ingredients →
Worker performs crafting animation → Worker produces output →
Output goes to building storage → Deliveryman moves to warehouse
```

### Tasks
- [ ] Implement `CraftingSimulator` — processes recipes from ICraftingBuildingModule
- [ ] Implement `FarmSimulator` — grows crops in assigned fields, harvests
- [ ] Implement `MineSimulator` — mines blocks at depth tier, deposits ores
- [ ] Implement `AnimalFarmSimulator` — breeds animals, collects drops
- [ ] Implement `SmelterSimulator` — processes furnace recipes
- [ ] Connect each simulator to the corresponding worker AI
- [ ] Track input/output statistics via ColonyStatisticsManager

---

## PRIORITY 5: Domum Ornamentum — Complete Block Set

### Current State
- Architects Cutter: WORKING (menu + screen fully functional)
- OrnamentBlock: Registered but all 20 types use same generic model
- Material texture system: MaterialTextureData stores data but no dynamic rendering

### Tasks
- [ ] Create proper block models for ALL 20 OrnamentBlockType variants:
  - [ ] Timber Frame (10 subtypes: plain, double_crossed, framed, etc.)
  - [ ] Framed Light (6 subtypes)
  - [ ] Shingle (5 height variants + slab)
  - [ ] Pillar (3 shape variants: round, voxel, square)
  - [ ] Panel, Post, Paper Wall (2 variants)
  - [ ] Door, Fancy Door, Trapdoor, Fancy Trapdoor
  - [ ] Extra Block (27 subtypes)
  - [ ] Barrel (2 variants)
  - [ ] All-Brick, Fence, Fence Gate, Slab, Wall, Stair
- [ ] Create blockstate files for each variant with proper facing/shape states
- [ ] Port DO's dynamic model baking system (MateriallyTexturedBakedModel)
  - [ ] IGeometryLoader for custom model loading
  - [ ] Quad transformation for retexturing
  - [ ] Cache with TTL for performance
- [ ] Create proper textures for all DO block types (port from DO source)
- [ ] Register all variant blocks (currently only 20 generic, need 100+)
- [ ] Wire each variant to the Architects Cutter recipe system

---

## PRIORITY 6: Missing GUI Windows (~80 windows)

### Critical Windows (must have for gameplay)
- [ ] `WindowHireWorker` — Select available citizen to assign to building
- [ ] `WindowBuildBuilding` — Browse buildings to construct, select style
- [ ] `WindowBuildingBrowser` — List all available building types
- [ ] `WindowResourceList` — Show required materials for construction
- [ ] `WindowRequestDetail` — Detailed view of a citizen's request
- [ ] `WindowClipBoard` — Overview of colony work orders
- [ ] `WindowPostBoxMain` — Player interface for request system
- [ ] `WindowRack` — Interact with storage rack contents
- [ ] `WindowGrave` — View citizen grave, recover items
- [ ] `WindowSupplies` — Supply camp/ship placement preview
- [ ] `WindowResearchTree` — Full research tree with branching visualization
- [ ] `WindowColonyMap` — Colony layout overview

### Important Windows (enhance experience)
- [ ] `WindowBannerRallyGuards` — Guard rally point management
- [ ] `WindowHutGuide` — In-game tutorial for each building
- [ ] `WindowHutNameEntry` — Rename dialog
- [ ] `WindowReactivateBuilding` — Reactivate deactivated building
- [ ] `WindowBuildingInventory` — View all items in a building
- [ ] `WindowCitizenInventory` — View citizen's personal inventory
- [ ] `WindowSettings` — Colony-wide settings
- [ ] `WindowStatsModule` — Detailed statistics view
- [ ] `WindowSupplyStory` — Lore text on first colony founding

### Town Hall Windows (8 variants)
- [ ] `WindowMainPage` — Main TH info
- [ ] `WindowInfoPage` — Colony info/stats
- [ ] `WindowCitizenPage` — Manage all citizens
- [ ] `WindowPermissionsPage` — Role-based permissions
- [ ] `WindowSettings` — Colony settings
- [ ] `WindowStatsPage` — Statistics overview
- [ ] `WindowAlliancePage` — Alliance management
- [ ] `WindowTownHallMercenary` — Hire mercenaries

### Building Module Windows (20+ specialized)
- [ ] `FarmFieldsModuleWindow` — Farm field management
- [ ] `PlantationFieldsModuleWindow` — Plantation management
- [ ] `GraveyardManagementWindow` — Grave/burial management
- [ ] `EnchanterStationModuleWindow` — Enchanter configuration
- [ ] `RestaurantMenuModuleWindow` — Restaurant menu management
- [ ] `MinimumStockModuleWindow` — Minimum stock configuration
- [ ] `EntityListModuleWindow` — Entity filter management
- [ ] `ItemListModuleWindow` — Item filter management
- [ ] `ToolModuleWindow` — Tool management
- [ ] `WorkOrderModuleWindow` — Work order management

### Citizen Windows
- [ ] `RequestWindowCitizen` — Citizen's active requests
- [ ] `FamilyWindowCitizen` — Family relationships
- [ ] `JobWindowCitizen` — Job details and stats

---

## PRIORITY 7: Request System Full Wiring

### Current State
- RequestManager exists with full lifecycle
- 4 resolvers exist (Warehouse, Crafting, Deliveryman, Player)
- DeliverymanResolver.canResolve() returns true but doesn't assign real citizens
- DeliverymanAI operates independently of RequestManager

### Tasks
- [ ] Wire buildings to create requests when they need materials
- [ ] Wire WarehouseResolver to actually check rack inventories
- [ ] Wire CraftingResolver to check crafting building recipe lists
- [ ] Wire DeliverymanAI to poll RequestManager for assigned deliveries
- [ ] Wire DeliverymanResolver to find nearest available deliveryman entity
- [ ] Implement request fulfillment flow: pickup from source → deliver to requester
- [ ] Wire PostBox block to show player-fulfillable requests
- [ ] Add request status sync to TownRequestsPanel

---

## PRIORITY 8: Research System Full Wiring

### Current State
- ResearchManager exists with startResearch/tickResearch/completeResearch
- GlobalResearchTree loaded from JSON datapacks
- ResearchEffectManager tracks active effects
- ResearcherAI calls tickResearch() (wired)

### Tasks
- [ ] Create more research JSON definitions (currently only 3 sample researches)
- [ ] Wire research effects to actual gameplay:
  - [ ] Worker speed multipliers
  - [ ] Building max level increases
  - [ ] Recipe unlocks
  - [ ] Guard damage bonuses
  - [ ] Disease immunity (vaccine research)
- [ ] Wire research requirements to check actual building levels
- [ ] Create full research tree visualization in TownResearchPanel
- [ ] Add research cost deduction (items from player inventory)

---

## PRIORITY 9: Guard/Patrol System

### Tasks
- [ ] Implement patrol point storage per guard building
- [ ] Implement patrol AI: walk between patrol points in sequence
- [ ] Implement guard mode: stay at position, attack hostiles in range
- [ ] Implement follow mode: follow assigned player
- [ ] Wire KnightGuardAI to use building's patrol settings
- [ ] Wire archer guards to use ranged combat from patrol positions
- [ ] Implement guard equipment management (auto-equip from building chest)

---

## PRIORITY 10: Colony Statistics & Advancements

### Tasks
- [ ] Wire ColonyStatisticsManager.increment() calls throughout all systems:
  - [ ] TREE_CUT in LumberjackAI
  - [ ] ORES_MINED in MinerAI
  - [ ] CROPS_HARVESTED in FarmerAI
  - [ ] ITEMS_CRAFTED in all crafting AIs
  - [ ] FOOD_SERVED in CookAI
  - [ ] CITIZENS_HEALED in HealerAI
  - [ ] FISH_CAUGHT in FishermanAI
  - [ ] BLOCKS_PLACED in BuilderAI
  - [ ] MOBS_KILLED on entity death events
  - [ ] BIRTH/DEATH on citizen spawn/death
- [ ] Wire advancement triggers to fire on milestones
- [ ] Create advancement JSON files for the full progression tree

---

## PRIORITY 11: Remaining Block Functionality

### Blocks That Need Implementation
- [ ] **BlockScarecrow** — Store assigned farmer UUID, define field boundaries
- [ ] **BlockDecorationController** — Store schematic name, apply decoration on interact
- [ ] **BlockGate** — Open/close with redstone, iron needs redstone, wood can be toggled
- [ ] **BlockWaypoint** — Register with pathfinding system for citizen navigation
- [ ] **BlockColonyBanner** — Display faction color, wall-mountable variant
- [ ] **BlockColonySign** — Display colony name text
- [ ] **BlockConstructionTape** — Visual boundary, non-solid, auto-placed by builder
- [ ] **BlockPlantationField** — Define plantation area for planter worker
- [ ] **BlockCompostedDirt** — Result of barrel composting, enhanced crop growth
- [ ] **BlockGrave / BlockNamedGrave** — Store dead citizen data, recoverable items

---

## PRIORITY 12: Citizen Lifecycle

### Tasks
- [ ] Implement citizen spawning when residence is built
- [ ] Implement citizen death → grave creation
- [ ] Implement citizen children (birth, growth, school enrollment)
- [ ] Implement visitor spawning at tavern
- [ ] Implement visitor recruitment flow
- [ ] Implement citizen happiness affecting work speed (wired but needs testing)
- [ ] Implement citizen disease spreading and treatment
- [ ] Implement citizen sleep cycle (beds in residence)

---

## PRIORITY 13: Structurize Tool Enhancements

### Tasks
- [ ] ShapeToolItem — Add save/place action (currently only previews)
- [ ] ScanToolItem — Add scan slot switching (10 slots in ScanToolData)
- [ ] BuildToolItem — Add rotation preview rendering
- [ ] TagToolItem — Add tag visualization overlay

---

## PRIORITY 14: Multi-Piston Enhancements

### Current: Working basic push/pull
### Needed:
- [ ] Block entity NBT preservation during push (save + restore tile entities)
- [ ] Entity displacement (push entities out of the way)
- [ ] Sound effects on push/pull
- [ ] Configurable max range in config

---

## PRIORITY 15: World Generation

### Current: Structure JSONs exist but some had format issues (fixed)
### Needed:
- [ ] Verify all 16 colony structure JSONs load without errors
- [ ] Verify all template pools resolve correctly
- [ ] Verify processor lists apply proper block replacement
- [ ] Test colony world generation in-game
- [ ] Test raider camp generation in-game

---

## Execution Order

1. **Server handlers** (Priority 1) — Makes existing screens functional
2. **Item functionality** (Priority 2) — Quick wins, each item is independent
3. **AI ↔ Building wiring** (Priority 3) — Connects the two systems
4. **Production chains** (Priority 4) — Makes colonies actually produce
5. **DO blocks** (Priority 5) — Visual content
6. **Missing GUIs** (Priority 6) — Batch implementation
7. Everything else (Priorities 7-15) — Incremental improvement

---

## Total Estimated Files to Create/Modify

| Priority | New Files | Modified Files | Total |
|----------|-----------|----------------|-------|
| 1 | 0 | 1 (TownHandler) | 1 |
| 2 | 10 | 1 (ColonyItemRegistry) | 11 |
| 3 | 2 | 44 (each AI) | 46 |
| 4 | 5 | 10 | 15 |
| 5 | 50+ | 5 | 55+ |
| 6 | 80 | 5 | 85 |
| 7 | 0 | 8 | 8 |
| 8 | 20 | 3 | 23 |
| 9 | 3 | 5 | 8 |
| 10 | 5 | 15 | 20 |
| 11 | 5 | 10 | 15 |
| 12 | 5 | 10 | 15 |
| 13 | 0 | 4 | 4 |
| 14 | 0 | 2 | 2 |
| 15 | 0 | 20 | 20 |
| **TOTAL** | **~185** | **~143** | **~328** |
