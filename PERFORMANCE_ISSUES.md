# MegaMod Performance Issues

Full audit of performance issues across the project, ranked by severity.

---

## Critical

### 1. AbilitySystem static maps never cleaned (memory leak)
**File:** `feature/relics/ability/AbilitySystem.java:44-48`
`ACTIVE_TOGGLES`, `INVULN_EXPIRY`, `LAST_HELD_ITEM`, `PLAYERS_WITH_ACTIVE_CDS` are ConcurrentHashMaps that grow per-player but are never cleaned on logout. Over a long server session these leak memory indefinitely.
**Fix:** Add `PlayerLoggedOutEvent` listener to evict entries for the disconnected player.

### 2. ComputerDataPayload 512 KB buffer
**File:** `feature/computer/network/ComputerDataPayload.java:15,19`
Reads up to 524,288 bytes per payload. Every computer screen sync can send half a megabyte of JSON. Causes network congestion and memory spikes on both client and server.
**Fix:** Paginate large data (wiki, recipes, etc.) or send incremental deltas instead of full state.

### 3. SharedMapManager file I/O on network thread
**File:** `feature/map/SharedMapManager.java:200`
`Files.readAllBytes(tileFile)` runs directly in the network handler. Multiple tile requests cause multiple blocking reads on the network thread, risking thread starvation.
**Fix:** Wrap file reads in `context.enqueueWork()` or use async I/O with a callback.

---

## High

### 4. Corruption system excessive sync packets
**File:** `feature/corruption/CorruptionEvents.java:340-357`
Sends `CorruptionSyncPayload` every 40 ticks AND `CorruptionZoneSyncPayload` every 100 ticks PER PLAYER. The zone sync also calls `getZonesInRange(pos, 256)` which does a spatial query. With 20 players this is 10+ packets/sec of corruption data.
**Fix:** Increase sync intervals to 100-200 ticks. Only send zone data when player moves >16 blocks since last sync. Use dirty flags.

### 5. HomingXP entity scan every 4 ticks per player
**File:** `feature/homingxp/HomingExperienceOrbs.java:34-56`
Calls `level.getEntitiesOfClass(ExperienceOrb.class, searchBox)` in a 24-block radius, 5 times per second per player. With 20 players that's 100 entity scans/sec.
**Fix:** Increase interval to every 10-20 ticks. Cache results. Reduce radius to 12-16 blocks.

### 6. Admin module entity scans without throttling
**File:** `feature/adminmodules/modules/combat/CombatModules.java:226,679,767,982`
Several inline `getEntitiesOfClass(Monster.class, ...)` calls run every tick with no throttle. These are boolean "is enemy nearby?" checks that could easily be cached for 4-10 ticks.
**Fix:** Add tick modulo checks (every 4-10 ticks) to all unthrottled entity scans.

### 7. ESP + Tracers entity scan every render frame
**File:** `feature/adminmodules/modules/render/ESPModule.java:83`, `TracersModule.java:50`
Both call `getEntitiesOfClass()` with 64-256 block radius EVERY FRAME (60+ times/sec). ESPModule also creates a new ArrayList for cached chests periodically.
**Fix:** Cache entity list for 4-5 frames. Use a frame counter and only rescan when stale.

### 8. KillAura entity scan + sort every tick
**File:** `feature/adminmodules/modules/combat/KillAuraModule.java:131-171`
Every tick: scans `LivingEntity` in 4.5-block radius, filters by 6+ predicates, then sorts by distance/health (O(n log n)). Even with vanilla cooldown gating attacks, the scan runs unconditionally.
**Fix:** Only scan on ticks where an attack could actually fire. Cache target for 2-4 ticks.

### 9. Cascading disk saves (11 managers at once)
**File:** `feature/citizen/CitizenEvents.java:144-156`
`saveAll()` calls `NbtIo.writeCompressed()` on 11 managers sequentially (CitizenManager, FactionManager, GroupManager, ClaimManager, DiplomacyManager, TreatyManager, etc.) every 5 minutes.
**Fix:** Stagger saves across multiple ticks (save 2-3 managers per tick over 5-6 ticks).

---

## Medium

### 10. AmbientSoundEngine synchronized lock held during full tick
**File:** `feature/ambientsounds/sound/AmbientSoundEngine.java:36-83`
Holds `synchronized(sounds)` for 25+ lines including TreeMap creation, iteration, and sound processing every client tick. Blocks the render thread.
**Fix:** Replace synchronized ArrayList with CopyOnWriteArrayList, or collect work outside the lock and apply briefly.

### 11. MapScreen waypoint panel O(n^2) indexOf in render loop
**File:** `feature/computer/screen/MapScreen.java:1108`
`this.waypoints.indexOf(wp)` called inside render loop for every visible waypoint. O(n) per row = O(n^2) total.
**Fix:** Cache original indices when building the filtered list, or use a Map<Waypoint, Integer>.

### 12. MapScreen filtering + sorting waypoints every frame
**File:** `feature/computer/screen/MapScreen.java` (getFilteredSortedWaypoints)
Rebuilds filtered/sorted waypoint list with distance calculations on every render call.
**Fix:** Cache filtered list. Rebuild only when search text, sort mode, or player position changes >10 blocks.

### 13. Casino chip balance sync every 10 ticks
**File:** `feature/casino/CasinoEvents.java:129-138`
Iterates ALL players every 0.5 seconds to check if they have chips, then syncs. With 50 players that's 100 iterations/sec.
**Fix:** Only sync when chip balance actually changes (event-driven), or increase interval to 60-100 ticks.

### 14. BackpackEvents iterates all players every 5 ticks
**File:** `feature/backpacks/BackpackEvents.java:41-59`
Every 5 ticks, iterates ALL online players, checks if wearing backpack, creates new UpgradeManager, initializes from stack, ticks upgrades, saves back. The manager creation per tick is wasteful.
**Fix:** Cache UpgradeManager per player. Only create on equip, destroy on unequip. Check `isWearing()` less frequently for non-wearing players.

### 15. EconomyManager global synchronized lock
**File:** `feature/economy/EconomyManager.java:125-186`
All wallet/bank methods (`addWallet`, `spendWallet`, `transferToBank`) are synchronized on the instance. Every coin transaction blocks all other transactions server-wide.
**Fix:** Use ConcurrentHashMap with `compute()` for per-player atomic operations instead of global lock.

### 16. Citizen entity class scan with world AABB
**File:** `feature/citizen/CitizenEvents.java:291`
`level.getEntitiesOfClass(AbstractCitizenEntity.class, worldBounds)` scans ALL citizens with an enormous AABB.
**Fix:** Use CitizenManager's registry to iterate known citizens directly instead of a spatial entity scan.

### 17. SortingManager O(n^2) mergeStacks
**File:** `feature/sorting/SortingManager.java:37-93`
`mergeStacks()` uses nested loops to combine partial stacks. O(n^2) with large containers.
**Fix:** Use a Map<ItemKey, List<Slot>> to group matching items in O(n), then merge within groups.

### 18. Corruption zone mob spawning in bulk
**File:** `feature/corruption/CorruptionEvents.java:138-171`
Every 400 ticks, spawns 2-10 mobs per active corruption zone all at once. Entity creation is expensive.
**Fix:** Spread spawns across multiple ticks (1 mob per tick over 10 ticks).

### 19. Attribute modifier refresh every second per player
**File:** `feature/attributes/AttributeEvents.java:241-306`
Every 20 ticks, adds/removes attribute modifiers (health regen, hunger, combo speed, jump height, swim speed, excavation reach) for each player. Modifier add/remove triggers recalculation.
**Fix:** Use longer-duration modifiers (600+ ticks) and only refresh when values actually change. Check dirty flags before applying.

### 20. FriendsScreen stream count in render
**File:** `feature/computer/screen/FriendsScreen.java:256`
`friends.stream().filter(FriendEntry::online).count()` runs every frame.
**Fix:** Cache online count, update when friend list changes.

---

## Low

### 21. AccessoryScreen try-catch around Identifier.parse every frame
**File:** `feature/relics/client/AccessoryScreen.java:154-163,194-200`
Wraps `Identifier.parse()` in try-catch for every equipped slot every frame. Add null/empty checks before parsing.

### 22. Multiple independent all-player iterations per tick
`AbilitySystem`, `BackpackEvents`, `CitizenEvents`, `AdminModuleEvents` each call `getPlayerList().getPlayers()` independently every tick. Consider a single tick dispatcher that iterates players once and dispatches to subsystems.

### 23. Gravestone I/O on first block entity tick
**File:** `feature/recovery/GravestoneBlockEntity.java:68-88`
Saves gravestone data to disk on the first tick of the block entity. Should use async I/O.

### 24. CitizenPatrolSpawn.tick() called every tick redundantly
**File:** `feature/citizen/CitizenEvents.java:69`
`CitizenPatrolSpawn.tick(level)` runs every server tick but internally just increments a timer. Gate the call with the interval check externally.

---

## Admin Module Entity Scans Explained

The admin modules system has 25+ modules that scan for nearby entities. Here's what each category does:

### Combat Modules (server-side, attack automation)
| Module | What it does | Scan radius | Frequency |
|--------|-------------|-------------|-----------|
| **KillAura** | Auto-attacks nearest living entities (mobs/players). Scans, filters (alive, non-admin, non-tamed), sorts by distance/HP, attacks up to N targets. | 4.5 blocks | Every tick |
| **CrystalAura** | Auto-places/detonates End Crystals near enemies. Scans for crystals to break AND targets to place near. | 4-6 blocks | Every 4 ticks |
| **BowAimbot** | Redirects player's arrows mid-flight toward nearest target. Scans for own arrows + potential targets. | 8-64 blocks | Every 2 ticks |
| **Criticals** | Auto-jumps when hostile mobs nearby to land critical hits. Boolean "enemy present?" check. | 4 blocks | Every tick (NO THROTTLE) |
| **AutoWeapon** | Switches to best weapon type when hostiles detected nearby. | 5 blocks | Every 4 ticks |
| **Hitboxes** | Teleports mobs slightly closer to extend effective melee reach. | 5 blocks | Every 2 ticks |
| **TriggerBot** | Auto-clicks attack when crosshair lines up with an entity (raycast-based). | 3-8 blocks | Every 2 ticks |
| **Quiver** | Auto-selects arrow type (piercing/fire/etc.) based on nearby target's armor. | 16 blocks | Every 10 ticks |

### Render Modules (client-side, visual overlays)
| Module | What it does | Scan radius | Frequency |
|--------|-------------|-------------|-----------|
| **ESP** | Draws wireframe boxes around entities (players/mobs/items). | 8-256 blocks | EVERY FRAME |
| **Tracers** | Draws lines from crosshair to all visible entities. | 8-256 blocks | EVERY FRAME |
| **Nametags** | Applies glowing effect to players/entities for visibility through walls. | 16-256 blocks | Every 40 ticks |
| **Chams** | Makes entities glow through blocks for wallhack-style visibility. | 8-64 blocks | Every 20 ticks |
| **SkeletonESP** | Highlights skeleton-type mobs specifically. | 16-128 blocks | Every 40 ticks |
| **ItemHighlight** | Makes dropped items glow for easier pickup. | 4-32 blocks | Every 20 ticks |
| **NoRender** | Removes visual clutter (dropped items, XP orbs, arrows) from view. | 8-64 blocks | Every 60 ticks |

### World Modules (server-side, automation)
| Module | What it does | Scan radius | Frequency |
|--------|-------------|-------------|-----------|
| **AutoBreed** | Feeds nearby animals to breed them. | 8 blocks | Every 100 ticks |
| **AutoShearer** | Shears nearby sheep automatically. | 8 blocks | Every 40 ticks |
| **EntityCleaner** | Removes non-player entities (item clutter, mobs). | 16 blocks | Every 100 ticks |
| **Flamethrower** | Ignites nearby food animals to cook drops. | 10 blocks | Every 10 ticks |
| **AutoMount** | Mounts nearest rideable entity (horse, pig). | 4 blocks | Every 20 ticks |
| **AutoNametag** | Applies nametags to unnamed mobs. | 4 blocks | Every 40 ticks |
| **EndermanLook** | Teleports away from angry endermen. | 16 blocks | Every 10 ticks |

### Player Modules (server-side, utility)
| Module | What it does | Scan radius | Frequency |
|--------|-------------|-------------|-----------|
| **AutoClicker** | Auto-attacks entity under crosshair (raycast). | 6 blocks | CPS-based |
| **ReachExtend** | Attacks entities beyond normal reach (raycast). | 5-12 blocks | Every 4 ticks |
| **AutoTrade** | Opens trade with nearest villager. | 3 blocks | Every 100 ticks |
| **SmartMine** | Pauses mining and switches to sword when hostile detected. | 4 blocks | Every 5 ticks |

### Misc Modules (server-side, info/social)
| Module | What it does | Scan radius | Frequency |
|--------|-------------|-------------|-----------|
| **Notifier** | Alerts when hostile mobs are nearby + player join/leave tracking. | 16 blocks | Every 40 ticks |
| **MessageAura** | Auto-greets nearby players. | 16 blocks | Every 20 ticks |

### Worst offenders for performance:
1. **ESP + Tracers**: Scan 256-block radius EVERY FRAME (60/sec)
2. **KillAura**: Scans + sorts EVERY TICK (20/sec)
3. **Criticals**: Unthrottled enemy check EVERY TICK
4. **BowAimbot + Hitboxes + TriggerBot**: Every 2 ticks (10/sec)
