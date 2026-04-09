# MegaMod - Claude Context

## Project Overview
All-in-one Minecraft mod ("MegaMod") for NeoForge. Combines QoL, admin tools, economy, combat, exploration, and colony management into a single mod. The user (Julian / "Drek") describes features and they get implemented here.

## Tech Stack
- **Minecraft**: 1.21.11
- **Mod Loader**: NeoForge 21.11.42
- **Gradle Plugin**: net.neoforged.moddev 2.0.140
- **Parchment Mappings**: 2025.12.20
- **Java**: 21 (via Gradle toolchain)
- **Mod ID**: `megamod`
- **Package**: `com.ultra.megamod`
- **Author**: Drek (Julian Calero)
- **License**: All Rights Reserved

## Build & Run
- JDK 21: Eclipse Temurin 21.0.10 at `C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot`
- Set before building: `export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot"`
- Build: `./gradlew build` -> `build/libs/megamod-1.0.0.jar`
- Run client: `./gradlew runClient` (4GB max heap, early window control disabled)
- Run server: `./gradlew runServer` (nogui)

## Key API Patterns (MC 1.21.11 / NeoForge 21.11)
- Use `Identifier` NOT `ResourceLocation` (`import net.minecraft.resources.Identifier`)
- `Identifier.fromNamespaceAndPath("megamod", "...")` for creating identifiers
- `@EventBusSubscriber(modid = MegaMod.MODID)` for game bus events (auto-registered)
- `@EventBusSubscriber` does NOT support `bus = Bus.MOD` — use `modEventBus.addListener(MyClass::method)` for mod bus events instead
- `@EventBusSubscriber(modid = MegaMod.MODID, value = Dist.CLIENT)` for client-only game bus
- `entity.level()` for entities, `event.getLevel()` for block/chunk events — cast: `(ServerLevel) player.level()`
- `player.getGameProfile().name()` not `.getName()` (GameProfile is now a record)
- `entity.spawnAtLocation((ServerLevel) entity.level(), itemStack)` — needs ServerLevel param
- `level.isClientSide()` method, not field
- `CompoundTag` getters: `tag.getIntOr(key, 0)`, `getBooleanOr(key, false)`, `getStringOr(key, "")`, `getCompoundOrEmpty(key)`, `tag.keySet()` not `getAllKeys()`
- `ResourceKey.identifier()` not `.location()`
- `SoundEvents.TOTEM_USE` not `TOTEM_OF_UNDYING`, `MobEffects.SPEED` not `MOVEMENT_SPEED`
- GUI layer render callbacks take `(GuiGraphics, DeltaTracker)` not `(GuiGraphics, float)`
- Mod bus GUI layer registration: `modEventBus.addListener(MyClass::onRegisterGuiLayers)` in MegaModClient
- Network payloads: `CustomPacketPayload` records with `StreamCodec`. `ClientPacketDistributor.sendToServer()` on client, `PacketDistributor.sendToPlayer()` on server
- `SavedData.Factory` is GONE — use static singletons with manual `NbtIo` file persistence instead
- `BlockEntityType` via `DeferredRegister<BlockEntityType<?>>` with `Registries.BLOCK_ENTITY_TYPE`
- All Block subclasses need `protected MapCodec<? extends YourBlock> codec()` override — use `simpleCodec()` for blocks without extra state
- Item models in 1.21.x need `assets/megamod/items/<name>.json` definition files (not just `models/item/`)
- Block model textures must be under `textures/block/` (block atlas), item model textures under `textures/item/` (item atlas)
- `Villager` class at `net.minecraft.world.entity.npc.villager.Villager` (subpackage)
- `Zombie` class at `net.minecraft.world.entity.monster.zombie.Zombie` (subpackage)
- No `@Override` on `keyPressed` or `mouseScrolled` in Screen subclasses — signatures changed in 1.21.11

## Core Architecture

### Entry Points
- `MegaMod.java` — Main mod class, registers 30+ registries and 14 network systems
- `MegaModClient.java` — Client entry point, registers 20+ GUI layers, keybinds, renderers, menu screens
- `Config.java` — Empty config stub (ModConfigSpec)

### Persistence Pattern
Static singletons with manual `NbtIo` file I/O. Each manager has `loadFromDisk(ServerLevel)`, `saveToDisk(ServerLevel)`, and a dirty flag. Files stored in `world/data/`. Async saving via `AsyncSaveHelper`.

### Design Decisions
- Admin usernames: `Set.of("NeverNotch", "Dev")` in `AdminSystem.java`
- Economy uses integer MegaCoins (no decimals)
- Computer is purely Screen-based (no AbstractContainerMenu), uses custom payloads
- All Vanilla Refresh features are independent — no shared state
- 17 client mixins + 1 common mixin in `com.ultra.megamod.mixin`

### Mixins (18 total)
**Common (1)**: FishingHookMixin
**Client (17)**: GameRendererMixin, CameraMixin, GuiMixin, ItemEntityRendererMixin, PlayerTabOverlayMixin, KeyboardInputMixin, LivingEntityMixin, MultiPlayerGameModeMixin, EntityRendererMixin, LevelRendererMixin, LightTextureMixin, MinecraftMixin, AbstractContainerScreenMixin, ContainerScreenSortMixin, OggAudioStreamMixin, SoundBufferLibraryAccessor, AvatarRendererMixin, ItemInHandRendererMixin

## Feature Systems (~1,157 Java files across 68 packages)

### Dungeons System (~164 files)
Tiered roguelike dungeons in pocket dimension. Bosses (Wroughtnaut, Frostmaw, Naga, Sculptor, etc.), 13+ mob types, room generation, loot quality system, quest tracking, leaderboards, New Game+, insurance subsystem. Party support — all party members enter together.

### Citizen System (~160 files)
MineColonies-inspired NPC system. Worker types (Lumberjack, Fisherman, Merchant, etc.), recruit types (Bowman, Crossbowman, Horseman, Captain, Commander, Assassin, etc.), AI systems (combat, pathfinding, dodge), territory/claim system, siege system, factions, formations, equipment management.

### Computer System (~114 files)
Craftable computer block + phone item with desktop GUI. 40+ network handlers, 17+ screens.

**Desktop Apps**: Messages, Shop, Bank, Stats, Skills, Recipes, Wiki, Music, Games, Notes, Map, Friends, Ranks, Mail, Party, Settings, Trade, Colony, Bounty, Arena, Challenges, Customize, Admin

**Admin Panel Tabs**: Dashboard, Players, World, Items, MegaMod, Economy, Skills, Audit, Item Editor, Entities, Terminal, Museum Mgr, Inv Viewer, Dungeons, Toggles, Moderation, Structures, Scheduler, Colonies, Bot Control, Mob Showcase, Modules, Warps, Citizens, Casino, Corruption, Marketplace, Alchemy, System, Deaths, Cleanup, Loot Tables, Aliases, Undo, Furniture, Search

**Computer Network Pattern**: `ComputerActionPayload` (client->server), `ComputerDataPayload` (server->client). Handlers in `network/handlers/`. Screens poll `ComputerDataPayload.lastResponse` in `tick()`.

### Relic & Accessory System (~104 files)
Accessories across 8 slot types (Head, Face, Hands, Feet, Back, Neck, Belt, Rings) with 78+ abilities. RPG weapon system with weapon effects. Research/reroll system, infusion manager. Keybinds: V=Accessories, R=Primary Ability, G=Secondary Ability.

### Casino System (~72 files)
5 casino games (Slots, Blackjack, Roulette, Craps, Baccarat, Wheel) in dedicated pocket dimension. Chip system with denominations, dealer/cashier NPCs, full game mechanics. Currency integration with economy.

### Bot Control / Baritone System (~70 files)
Server-side pathfinding AI for admin-controlled player bots. A* pathfinding, 17+ processes (GoTo, Mine, Build, Chest, Farm, Fishing, Patrol, Follow, Explore, Elytra, Quarry, Backfill, Tunnel, Combat), 12 movement types, mob avoidance, path visualization.

### Backpacks System (~43 files)
Portable inventory with tiers/variants. 8+ upgrade types (Magnet, AutoPickup, Crafting, Feeding, Jukebox, Refill, Tanks, Void, Smelting). Wearable with layer renderer. Menu/screen system.

### Ambient Sounds System (~42 files)
Environmental audio engine. Biome/time/volume conditions, block/entity environments, dimension-aware, air pocket detection, region management, configurable sound engine.

### Alchemy System (~38 files)
AlchemyCauldronBlock, AlchemyGrindstoneBlock, 20+ custom potion effects (BerserkerRage, Inferno, VoidWalk, etc.), recipe registry, shelf block. Full crafting pipeline.

### Admin Modules System (~36 files)
25+ modules in categories: Combat (KillAura, BowAimbot, CrystalAura, Surround), Movement (Flight, Speed, Scaffold), Player (AutoFish, ChestStealer, AutoEat), Render (ESP, HoleESP, OreESP, Tracers, Trajectories), World (Nuker, VeinMiner). Per-module settings with NbtIo persistence. Admin-only.

### Skill Tree System (~35 files)
5 trees (Combat, Mining, Farming, Arcane, Survival), branches, nodes. Prestige system, synergy bonuses, capstone managers, challenges, skill locks (use locks + generation locks), badges, party buffs.

### Museum System (~30 files)
Animal Crossing-inspired collection building. 5 catalog wings (Wildlife, Art, Item, Achievement, Aquarium), Curator NPC, pocket dimension with portal, masterpiece paintings, pedestal displays.

### HUD System (~28 files)
20+ overlay layers organized by priority: background effects (vignette, transitions, corruption), world-space (mob health, compass, clocks), game info (status effects, quest tracker, crop maturity, skills), combat (abilities, combo text, kill combos, arena waves), social (party health, citizen), economy (player info, loot pickup, death recap), debug (bot paths), schematic.

### Schematic System (~24 files)
Structure loading, placement with transforms, material list calculator, build order/progress tracking, ghost renderer, placement mode, overlay renderer, browser screen.

### Dimensions / Pocket System (~16 files)
Shared infrastructure for Museum, Dungeon, Casino, and Resource dimensions. BlankChunkGenerator, PocketManager, PocketBuilder, DimensionHelper, PortalBlock/Entity, ResourceDimensionManager.

### Economy System (~15 files)
Wallet (lost on death) + Bank (safe) + MegaShop + ATM block. Economy analytics tracking. Furniture shop. Guide book helper.

### Arena & Challenges System (~14 files)
Competitive PvP arena with wave spawning, boss rush mode, leaderboards, checkpoints. Arena builder.

### Furniture System (~13 files)
Decorative blocks with custom VoxelShapes. SittableFurnitureBlock, SleepableFurnitureBlock, ComputerFurniture, TrashBinBlock, DungeonChest. Quest board system.

### Corruption System (~12 files)
Corruption mechanic with spread handler, purge system, zone sync, boundary rendering. Admin panel controls.

### Map System (~10 files)
Shared map with waypoint sync, beacon rendering, tile system, toolbar, drawing overlay.

### Villager Trade Refresh (~7 files)
Villager trade mechanics with admin trade editor.

### Attributes System (~7 files)
Custom entity attributes via DeferredRegister with helper/tooltip/event classes.

### Marketplace System (~6 files)
Player-to-player listing/trading via TradingTerminalBlock.

### Recovery / Gravestone System (~6 files)
Player death gravestone blocks with inventory recovery and coordinate tracking.

### Multiplayer (~6 files)
Join/exit sounds, player statistics, tab display, belowname kills, player list command.

### Sorting System (~5 files)
Container sorting with algorithm, keybind, and network sync.

### Prestige System (~4 files)
Mastery marks, prestige rewards, particles, death handling.

### Smaller Systems (1-3 files each)
Mob Variants, Dynamic Lights, Animations, Audit/Logging, Bounty Hunt, Death Sounds, Head Drops, Subtitles, Loot Modifiers, Feature Toggles, Moderation, Command Scheduler, Encyclopedia/Discovery, Tooltips

### Vanilla Refresh QoL Features (1 file each)
Player Sitting, Path Sprinting, Equipable Banners, Totem in Void, Craft Sounds, Readable Clocks, Better Armor Stands, Drop Ladders, Better Lodestones, Invisible Frames, Block Animations, Homing XP, Mob Health Display, Low Health Vignette, Elytra Flight, Echo Shard, Crops XP, Day Counter, Baby Zombie, Piglins, Party Cake, Griefing Protection, Spectator, Tree Felling, Trident, Jukebox

## Resource Patterns

### Textures
Generated programmatically with Python/PIL. 16x16 or 32x32 RGBA PNGs.

### Block Models
Blockbench-style JSON. Block space 0-16 units. Required files per block: blockstates JSON, models/block JSON, items JSON, textures/block PNG, lang entry.

### Resource Structure
- `assets/megamod/` — Main mod assets (blockstates, items, lang, models, sounds, textures, ambientsounds config)
- `assets/dungeonnowloading/` — Dungeon-specific models/textures/blockstates (140+ models)
- `assets/dynamiclights/` — Dynamic light language files
- `data/megamod/` — Advancements, dimensions, jukebox songs, loot modifiers/tables, painting variants, recipes, structures, worldgen (~300 files)
- `data/dungeonnowloading/` — Dungeon loot tables and worldgen template pools
- `data/dynamiclights/` — MCFunction scripts, advancements, predicates, tags (~503 files)
- `blueprints/` — ~9,914 structure/schematic files for in-game building templates

## Reference Code (`Ref Code/` directory)
Reference implementations from other mods, adapted for MegaMod's architecture.
