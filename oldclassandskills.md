# Old Class & Skill System — Reference Archive

> This document captures the complete design of MegaMod's pre-overhaul skill tree, class selection, and lock systems before they are removed. Use as a reference when designing the new system.
>
> **Status:** Retired. All systems documented here have been deleted from the codebase.

---

## 1. Skill Trees

### Five Trees (`SkillTreeType` enum)

| Tree | Abbrev | Radial angle | Theme |
|------|--------|-------------:|-------|
| `COMBAT` | CB | 270° | Mastery of weapons and warfare |
| `MINING` | MI | 342° | Expertise in excavation and ore extraction |
| `FARMING` | FA | 54° | Agriculture and animal husbandry |
| `ARCANE` | AR | 126° | Magical forces and relics |
| `SURVIVAL` | SV | 198° | Endurance, exploration, environmental mastery |

- **Level cap:** 50 per tree
- **XP curve:** `xpForLevel(n) = 100 + n * 50` (linear)
- **Radial UI:** Trees arranged at fixed angles around a central hub, each with its own bright color + dim color pair.

### Thirty Branches (`SkillBranch` enum)

Each tree has ~5 "classic" branches plus 0–2 "class archetype" branches (marked **[CLASS]**) added in the Phase 4 Combat Overhaul.

#### Combat Branches
- **BLADE_MASTERY** — Increase melee damage with bladed weapons
- **RANGED_PRECISION** — Improve critical strike chance with all attacks
- **SHIELD_WALL** — Enhance armor and evasion
- **BERSERKER** — Drain life and attack faster
- **TACTICIAN** — Amplify critical hit damage
- **PALADIN** *[CLASS]* — Holy damage, healing, divine shields
- **WARRIOR** *[CLASS]* — Berserker melee DPS, war cries, charges

#### Mining Branches
- **ORE_FINDER** — More mining XP from excavation
- **EFFICIENT_MINING** — Break blocks faster
- **GEM_CUTTER** — More loot fortune from mining
- **TUNNEL_RAT** — Reduced fall damage underground
- **SMELTER** — More MegaCoins from ores

#### Farming Branches
- **CROP_MASTER** — More farming XP from harvests
- **ANIMAL_HANDLER** (display "Rancher") — Animal XP + breeding bonus
- **BOTANIST** (display "Herbalist") — Hunger efficiency and foraging
- **COOK** — Passive health regen
- **FISHERMAN** (display "Master Fisherman") — Fishing fortune

#### Arcane Branches
- **RELIC_LORE** — Boost equipped relic ability power
- **ENCHANTER** — More arcane XP from magical activities
- **MANA_WEAVER** — Reduced relic cooldowns
- **SPELL_BLADE** — Increased magic damage
- **SUMMONER** — Amplified overall ability power
- **WIZARD** *[CLASS]* — Elemental magic, arcane mastery

#### Survival Branches
- **EXPLORER** — More XP from exploration
- **ENDURANCE** — Increased max health
- **HUNTER_INSTINCT** — Bonus combat XP from kills
- **NAVIGATOR** — Faster on foot
- **DUNGEONEER** — More dungeon loot drops
- **ROGUE** *[CLASS]* — Evasion, stealth, speed
- **RANGER** *[CLASS]* — Ranged damage, traps, wilderness

### Skill Nodes

`SkillNode` record fields: `id, branch, tier, cost, displayName, description, prerequisites, bonuses`.

Every branch follows the same **4-tier progression**:

| Tier | Point cost | Role |
|-----:|-----------:|------|
| 1 | 1 | Basic stat bonus |
| 2 | 2 | Enhanced bonus + secondary effect |
| 3 | 3 | Branch signature perk |
| 4 | 5 | Capstone (triggers active ability) |

Each tier strictly requires the previous one. Total node cost for a full branch = 1+2+3+5 = **11 points**.

**Example — BLADE_MASTERY**

| Tier | Node | Effect |
|-----:|------|--------|
| 1 | `blade_mastery_1` — Sharpened Edge | +1.5 attack damage |
| 2 | `blade_mastery_2` — Honed Blade | +2.0 attack damage; 15% no-durability-loss |
| 3 | `blade_mastery_3` — Master Swordsman | +3.0 attack damage, +3 armor shred; 30% no-durability |
| 4 | `blade_mastery_4` — Legendary Blade | +4 attack damage, +3 armor shred. **Capstone: Executioner** — instantly execute non-boss mobs below 15% HP |

### Tracked Stat Keys

Node `bonuses` maps key → value. Known keys:

- Vanilla: `attack_damage`, `armor`, `max_health`, `movement_speed`, `attack_speed`
- Custom (MegaMod attributes): `critical_chance`, `critical_damage`, `ability_power`, `spell_haste`, `spell_damage`, `healing_power`, `ranged_damage`, `lifesteal`, `dodge_chance`, `evasion`, `loot_fortune`, `fall_damage_reduction`, `stun_chance`, `thorns_damage`, `armor_shred`, `excavation_reach`, `mining_xp_bonus`, `mining_speed_bonus`, `farming_xp_bonus`
- Synergy-only (event-driven, not applied as attribute): `first_hit_crit`, `ore_mining_haste`, `kill_streak_bonus`

---

## 2. Skill Points — Flow & Caps

1. **Earning**: each tree level-up grants **+1 skill point** for that tree (max 50 levels ⇒ 50 pts/tree).
2. **Spending**: spend on a node; bonuses apply instantly.
3. **Branch cap**: a tree allows **2 branches fully invested by default**, **3 branches at prestige ≥ 3**.
4. **Respec**: admin command or consumable item; full point refund within one tree.
5. **Prestige**: reset tree to level 1, prestige counter +1, gain +2% per prestige to the tree's primary stat. Cap: 5 prestiges/tree.

---

## 3. Attribute Application Pipeline

`SkillAttributeApplier.recalculate(player)` runs this pipeline whenever unlocked state changes:

1. **Phase 1 — Group.** Iterate every unlocked node, collect `{attribute → {tree → sum}}`.
2. **Phase 2 — Diminishing returns.** Per-attribute, sort tree contributions descending and scale:
   - 1st tree: ×1.0
   - 2nd tree: ×0.75
   - 3rd tree: ×0.50
   - 4th+ tree: ×0.25
3. **Phase 3 — Prestige multiplier.** `total *= 1.0 + prestigeLevel * 0.02`.
4. **Phase 3.5 — Synergy merge.** Merge active synergy bonuses that map to attributes. Event-triggered synergies (kill-streaks, etc.) are handled separately in `SynergyEffects`.
5. **Phase 4 — Passive Mastery.** Unspent points in a tree grant **+0.5 primary stat each**, capped at 10 unspent (+5 max). Primary stats: COMBAT → `attack_damage`, MINING → `mining_speed_bonus`, FARMING → `farming_xp_bonus`, ARCANE → `ability_power`, SURVIVAL → `movement_speed`.
6. **Phase 5 — Apply via `AttributeModifier`.** Flat `ADD_VALUE` for most stats; `ADD_MULTIPLIED_BASE` for `movement_speed` (base 0.1 / 100). Cached in `SkillAttributeApplier.lastComputedBonuses`.

Recalculation triggers: node unlock, respec, prestige, login (re-apply transient modifiers).

---

## 4. Synergy Bonuses

Cross-branch passives — require nodes unlocked in both branches listed.

| Synergy | Branches | Effect |
|---------|----------|--------|
| Sharpshooter | RANGED_PRECISION + TACTICIAN | First hit on a new target crits for 1.5× |
| Prospector's Rush | ORE_FINDER + EFFICIENT_MINING | Mining ore grants 2s Haste I |
| Adventurer | EXPLORER + DUNGEONEER | +10% movement speed in the dungeon dimension |
| Undying | ENDURANCE + COOK | Heal 1 HP/s while below 30% HP |
| Iron Fortress | SHIELD_WALL + ENDURANCE | Resistance I while below 50% HP |
| Hawk Eye | RANGED_PRECISION + HUNTER_INSTINCT | +15% ranged damage when can-see-sky |
| Calculated Hunter | HUNTER_INSTINCT + TACTICIAN | Kill-streak stacks +5% dmg/stack (max 5, 10s window) |
| Underground Express | EFFICIENT_MINING + TUNNEL_RAT | Speed I below Y=50 |

`SynergyManager` static registry (node-set → synergy); `SynergyEffects` implements each via event listeners.

---

## 5. Capstone Abilities (Tier-4 Nodes)

**COMBAT** (`CombatCapstones.java`)
- **Executioner** (blade_mastery_4) — Execute non-boss mobs <15% HP
- **Deadeye** (ranged_precision_4) — +50% projectile damage while not sprinting
- **Fortress** (shield_wall_4) — Reflect 30% blocked damage + Slowness I on attacker
- **Undying Rage** (berserker_4) — Cheat death once / 5 min + Resistance + Strength 3s

**MINING** (`MiningCapstones.java`), **FARMING** (`FarmingCapstones.java`), **ARCANE** (`ArcaneCapstones.java`), **SURVIVAL** (`SurvivalCapstones.java`) — analogous tier-4 active effects per branch. Each file holds the effect logic plus the trigger event handler.

`CapstoneManager` tracks which capstones are unlocked for trigger checks.

---

### 6a. Use Locks (`SkillLockDefinitions.USE_LOCKS`)

Player may hold a locked item but can't swing/use it without the required branch. Each lock: `{ category, Set<itemPattern>, branchA, branchB? }`. Either branch satisfies the lock.

**Melee weapons**
- Swords (diamond, netherite, unique_longsword) → BLADE_MASTERY | TACTICIAN
- Daggers (unique_dagger_*, naga_fang_dagger, ghost_fang) → BLADE_MASTERY | TACTICIAN
- Claymores (unique_claymore_*) → BLADE_MASTERY | BERSERKER
- Axes (diamond, netherite, unique_double_axe_*, wrought_axe) → BERSERKER | BLADE_MASTERY
- Hammers (terra_warhammer, unique_hammer_*) → BERSERKER | SHIELD_WALL
- Maces (mace, unique_mace_*, earthrend_gauntlet) → BERSERKER | SHIELD_WALL
- Glaives (unique_glaive_*, crescent_blade, battledancer) → TACTICIAN | BLADE_MASTERY
- Sickles (unique_sickle_*) → TACTICIAN | BERSERKER

**Ranged**
- Longbows (unique_longbow_*) → RANGED_PRECISION | HUNTER_INSTINCT
- Heavy Crossbows (unique_heavy_crossbow_*) → RANGED_PRECISION | HUNTER_INSTINCT
- Spears (unique_spear_*, trident) → RANGED_PRECISION | HUNTER_INSTINCT
- Thrown (blowgun, dart, spore_sack, blazing_flask) → RANGED_PRECISION | EXPLORER
- Tipped Arrows (tipped_arrow, cerulean_arrow, crystal_arrow) → RANGED_PRECISION | BOTANIST

**Magic**
- Damage Staves (unique_staff_damage_*, static_seeker, ebonchill) → SPELL_BLADE | MANA_WEAVER
- Healing Staves (unique_staff_heal_*, lightbinder) → MANA_WEAVER | SUMMONER
- Tomes & Scepters (vampiric_tome, scepter_of_chaos) → SPELL_BLADE | RELIC_LORE

**Defense**
- Unique Shields (unique_shield_*) → SHIELD_WALL | ENDURANCE
- Netherite Armor (helmet/chest/legs) → SHIELD_WALL | ENDURANCE
- Netherite Boots → ENDURANCE | NAVIGATOR

**Tools / consumables / storage**
- Pickaxes (diamond, netherite) → EFFICIENT_MINING | SMELTER
- Shovels / Hoes → CROP_MASTER branch-locked
- Golden Apple / Totem of Undying → COOK | ENDURANCE / ENDURANCE | BERSERKER
- Ender Pearl / Ender Chest → EXPLORER | MANA_WEAVER
- Shulker Boxes → DUNGEONEER | EXPLORER

**Class archetype locks** — some item categories additionally check `PlayerClassManager.classAllowsBranch(playerId, branchA, branchB)` so only matching class can use them regardless of branch investment.

### 6b. Generation Locks (`SkillLockDefinitions.ENCHANT_LOCKS`)

Enchantments don't appear on that player's enchanting tables, loot rolls, or chest loot. Another player can still trade the enchanted item.

- **Exclusive locks** (no Enchanter bypass):
  - Fortune I–III → GEM_CUTTER | ORE_FINDER
  - Efficiency III+ → EFFICIENT_MINING | ORE_FINDER
  - Silk Touch → ORE_FINDER | EFFICIENT_MINING
  - Looting I–III → TACTICIAN | HUNTER_INSTINCT
  - Luck of the Sea I–III → FISHERMAN only
  - Lure I–III → FISHERMAN only
  - Mending → ENCHANTER | MANA_WEAVER
  - Infinity → RANGED_PRECISION only
  - Sharpness V → BLADE_MASTERY only
  - Power V → RANGED_PRECISION only
- **Standard locks** allow ENCHANTER as a second path at max branch tier.

### 6c. Enforcement

- `SkillLockManager.canUse(player, stack)` — returns Tristate, checked by events.
- `SkillLockEvents` — listens on `ItemUseFirst`, `EntityInteract`, bow draw, etc. Cancels the action + sends actionbar if locked.
- `SkillLockTooltips` — client injects "Requires: [branch]" tooltip line with progress color.
- **Admin bypass**: `FeatureToggleManager` toggles `admin_skill_lock_bypass` (only NeverNotch/Dev) and master `skill_item_locks`.
- **Economy intent**: miners supply Fortune/Silk/Efficiency gear to the server; rangers supply Power V bows; blade masters supply Sharpness V; fishermen monopolize Luck of the Sea/Lure; arcane players control Mending.

---

## 7. Prestige System

- `PrestigeManager` — per-player, per-tree integer counter (0–5).
- **Base bonus:** +2% per prestige to the tree's primary stat (applied in Phase 3 of attribute pipeline).
- **Third-branch unlock:** at prestige ≥ 3, a tree allows specializing in a 3rd branch.
- **Class-specific prestige bonuses** (`PrestigeClassBonusHandler`) — only apply if `PlayerClass` matches the tree:

| Class | Tree | Bonus per prestige level |
|-------|------|--------------------------|
| PALADIN | COMBAT | +5% HEALING_POWER |
| WARRIOR | COMBAT | +5% CRITICAL_DAMAGE |
| WIZARD | ARCANE | +5% SPELL_HASTE |
| ROGUE | SURVIVAL | +5% DODGE_CHANCE |
| RANGER | SURVIVAL | +5% RANGED_DAMAGE |

Applied as transient `AttributeModifier`s → reapplied on every login in `ClassEventHandler.onPlayerLogin`.

Persisted to `megamod_prestige.dat` via NbtIo.

---

## 8. Challenges (`SkillChallenges.java`)

Optional per-branch objectives. Framework only — content is admin-definable:
- "Kill 100 mobs with [branch]" → bonus tree XP
- "Mine 1000 ores with [branch]" → bonus MegaCoins
- "Collect 50 items with [branch]" → bonus loot roll

Challenges track progress; completion fires a reward event.

---

## 9. Cosmetics & Display

- **Skill Badges** (`SkillBadges`) — auto-generated `⭐⭐ [Branch]` prefix based on highest branch tier. Admin can override per UUID. Persisted.
- **Prestige stars** — chat + tab list prefix, one ★ per prestige level (capped 25 total).
- **Name coloring** — prestige 5+ yellow, 15+ gold, 25+ red.
- **Skill Cosmetics** (`SkillCosmetics`) — particles/sounds on node unlock and capstone trigger (soul particles on Executioner kill, CRIT particles on Fortress reflect, etc.).
- **HUD overlay** (`SkillHudOverlay`) — top-center "+X [Tree] XP" toast, 3s display, fade at 2s. Pushed from `SkillEvents.onMobKill → showXpGain`.
- **EquipmentStatsOverlay** (relics feature) — displays player's total attribute bonuses per tree, color-coded.

---

## 10. XP Sources (`SkillEvents`)

Event handlers award XP by tree. Anti-abuse: per-player per-event-type cooldown; global cap of **750 XP/min/tree**.

- **COMBAT** — mob kills (scaled by mob type / boss flag)
- **SURVIVAL** — dungeon mob kills, travel distance, sleep in new biome
- **ARCANE** — dungeon mob kills (shared with SURVIVAL), relic ability triggers, enchanting
- **MINING** — block break on ore/stone families, deepslate variants
- **FARMING** — crop harvest, animal breed, fishing (`FishingHookMixin` bonus for FISHERMAN)
- **All** — quest reward grant, tree-felling (FARMING), NG+ multipliers

`SkillEvents.applyXpBonus` multiplies raw XP by:
- Admin global multiplier (SkillManager.adminXpMultiplier, 1.0–10.0)
- Admin-only boost if player is admin (SkillManager.adminOnlyXpBoost, 1.0–10.0)

---

## 11. Admin Integration

### `ComputerActionHandler` (server-side)
- `skill_add_xp` → `SkillManager.addXp(player, tree, amount)`
- `skill_add_points` → direct skill point grant
- `skill_set_level` → sets tree level directly (recalculates bonuses)
- `skill_reset_tree` → clears unlocked nodes in a tree, refunds points
- `skill_max_all_trees` → sets all 5 trees to level 50
- `skill_set_admin_xp_mult` → sets `SkillManager.adminXpMultiplier`
- `skill_set_admin_only_xp_boost` → sets `SkillManager.adminOnlyXpBoost`
- `admin_class_change` → `PlayerClassManager.setClass(uuid, cls)`
- `admin_class_list` → dumps all UUID → class assignments

### `AdminTerminalScreen` (client-side)
- Skills tab — per-tree level sliders, point grants, reset buttons, XP multiplier inputs
- Classes tab (or player-detail subtab) — shows each player's class, drop-down to reassign

### Commands (`SkillCommands.java`)
- `/skill add <player> <tree> <xp>`
- `/skill set <player> <tree> <level>`
- `/skill reset <player> <tree>`

---


### Selection Flow

1. `ClassEventHandler.onPlayerLogin` — checks `PlayerClassManager.hasChosenClass(uuid)`. If false, queues player in `PENDING_SELECTION` map with current gameTime.
2. On server tick, entries that have waited ≥60 ticks (3s) are drained — server sends `ClassSelectionPayload` → client.
3. `ClassSelectionScreen` opens fullscreen, non-dismissible. 5 class cards (color-coded), confirm button disabled until a class is selected.
4. User clicks Confirm → `ClassChoicePayload` (C2S) with class name.
5. Server validates, calls `PlayerClassManager.setClass(uuid, class)`, persists.
6. Server sends `ClassSyncPayload` to all clients for HUD display.
7. `PrestigeClassBonusHandler.applyClassPrestigeBonuses(player)` fires (transient modifiers).
8. Subsequent logins: if `hasChosenClass`, just sync + reapply prestige bonuses.

### What the class choice gates

| System | Effect |
|--------|--------|
| `SkillLockManager.classAllowsBranch` | Class-locked items require matching class (beyond the branch check) |
| `SpellUnlockManager` + `SpellScrollItem` | What spells player can learn/cast natively; other-class spells require permanent learn via scroll |
| `DungeonLootGenerator` | Loot weighted toward class (Paladin → more healing staves, Rogue → more daggers) |
| `QuestsHandler` | Class-specific quest lines + rewards |
| `SkillTreeScreen` | Class branch highlighted / non-class archetype branches visually restricted |
| `SkillEvents` | Class-matched activities get modified XP |
| `PrestigeClassBonusHandler` | Prestige-only attribute bonuses (see §7) |

### Persistence

- `megamod_player_classes.dat` — UUID → PlayerClass name (+ learned spell IDs set per UUID)
- Loaded once on server start via `PlayerClassManager.get(overworld).loadFromDisk(...)`
- Saved on dirty via `ComputerEvents.onPlayerLogout` + `onServerStopping`

---

## 13. File Inventory (as of removal)

### `feature/skills/`
- `SkillTreeType.java`, `SkillBranch.java`, `SkillNode.java`, `SkillTreeDefinitions.java`
- `SkillManager.java`, `SkillEvents.java`, `SkillAttributeApplier.java`, `SkillCommands.java`, `SkillBadges.java`, `SkillCosmetics.java`, `SkillPartyBuffs.java`, `BranchEffects.java`
- `locks/SkillLockDefinitions.java`, `SkillLockManager.java`, `SkillLockEvents.java`, `SkillLockTooltips.java`
- `prestige/PrestigeManager.java`, `PrestigeClassBonusHandler.java`
- `synergy/SynergyManager.java`, `SynergyEffects.java`
- `capstone/CapstoneManager.java`, `CombatCapstones.java`, `MiningCapstones.java`, `FarmingCapstones.java`, `ArcaneCapstones.java`, `SurvivalCapstones.java`
- `challenges/SkillChallenges.java`
- `client/SkillTreeScreen.java`, `SkillNodeWidget.java`, `SkillTreeKeybind.java`, `SkillHudOverlay.java`
- `network/SkillNetwork.java`, `SkillSyncPayload.java`, `SkillActionPayload.java`
- `integration/SkillsEconomyIntegration.java`, `SkillsRelicIntegration.java`

### `feature/combat/` (class-selection parts)
- `PlayerClassManager.java`, `ClassEventHandler.java`
- `client/ClassSelectionScreen.java`, `client/ClassSelectionClientProxy.java`, `client/ClientClassCache.java`
- `network/ClassNetwork.java`, `ClassSelectionPayload.java`, `ClassChoicePayload.java`, `ClassSyncPayload.java`
- `ClassDeathMessageHandler.java`, `ClassCommands.java`

### Persistence files removed with system
- `world/data/megamod_skills.dat`
- `world/data/megamod_prestige.dat`
- `world/data/megamod_player_classes.dat`

### External caller cleanup
70+ files referenced one or more of the above — all cleaned up during removal. Notable ones:
- `ComputerActionHandler` — stripped skill + class admin actions
- `AdminTerminalScreen` — removed Skills + Class tabs
- `SkillsAppScreen` (computer app) — deleted
- `DungeonLootGenerator` — dropped class-weighting branch
- `QuestsHandler`, `QuestRewardGranter`, `QuestTaskEvaluator` — removed class gating + skill-XP rewards
- `SpellUnlockManager`, `SpellScrollItem` — class check removed (behavior choice during overhaul)
- `FishingHookMixin` — fisherman bonus stripped
- `WorldLootIntegration`, `RecipeUnlocker` — lock checks removed
- `PlayerStatistics`, `BelownameDisplay`, `TabDisplay` — badge display removed
- `EquipmentStatsOverlay` — tree-grouped stat display removed
- `CitizenSkillBonuses` — colony citizen inheritance removed

---

## 14. Design Intent (for the overhaul)

Notes from the original design, useful when planning the replacement:

- **Branch specialization creates economy.** Because generation locks are player-scoped, different specializations produce different enchanted gear, encouraging trade.
- **Classes are keys, not stat bundles.** Picking a class mostly unlocks access (weapons, spells, loot weighting). Direct stat buffs are back-loaded behind prestige.
- **Prestige is the long-tail.** Base level caps at 50 quickly; prestige extends the horizon another 5 cycles per tree with permanent bonuses.
- **Synergy rewards horizontal investment.** Players who spread across two adjacent branches get unique effects impossible to replicate with depth alone.
- **Admin multipliers let servers tune pacing** without changing code (`/skill set adminXpMultiplier 3.0`).
- **Capstones are the "I'm a real X now" moment** — active mechanics, not just numbers.
