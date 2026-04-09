# MegaMod Ideas

> Completed features are tracked in CLAUDE.md. This file tracks design intent, art style, and future expansion ideas.

---

## Art Style Guide — Medieval RPG

> All MegaMod UI, items, blocks, and assets must follow this style. Reference: `Example images/` folder and resource pack folder

### Visual Identity
MegaMod's visual identity is **ornate medieval fantasy RPG** — rich, layered, and tactile. Think enchanted guild hall, not flat modern app. Every UI element should feel like it was crafted by a fantasy blacksmith from gold, leather, and cloth.

### Core Elements

| Element | Style |
|---------|-------|
| **Frames/Borders** | Ornate gold with 3D beveled edges — bright gold highlights on top/left, dark gold shadow on bottom/right. Decorative corner scrollwork/flourishes at each corner. Multi-layered: outer shadow → gold border → inner trim → content |
| **Backgrounds** | Dark brown leather/parchment with subtle texture variation (not flat solid color). Should look like aged leather with slight grain and color shifts |
| **Accent Color** | Royal blue cloth/ribbon accents — used for banners, tab highlights, active states, and decorative hanging elements |
| **Slots/Cells** | Darker brown inset squares with subtle inner shadow, bordered by thin gold trim |
| **Buttons** | Beveled gold-framed rectangles with leather fill. Normal: muted gold border + dark leather. Hover: bright gold border + lighter leather. Should feel "pressable" with 3D depth |
| **Tabs** | Active: gold-framed with leather fill, connected to panel below. Inactive: dark, recessed, muted |
| **Text** | Gold/amber colored with dark outlines for headings. Light cream/parchment color for body text. Important values in bright gold |
| **Progress Bars** | Gold-framed trough with colored fill (green→yellow→red gradient for health, solid gold for XP) |
| **Icons** | Beveled, with subtle drop shadows — never flat. Match the gold-and-leather palette |
| **Decorations** | Blue cloth ribbons, gold medallions, corner scrollwork, rivets along edges |

### Color Palette

| Name | Hex | Usage |
|------|-----|-------|
| Gold Bright | `#FFD700` | Highlights, corner flourishes, active borders |
| Gold Mid | `#DAA520` | Standard gold borders, text headings |
| Gold Dark | `#C8A84C` | Secondary borders, beveled edges |
| Gold Shadow | `#8B6914` | Bottom/right bevel shadows |
| Gold Deep | `#5A4A0A` | Outermost frame shadow |
| Leather | `#4A2A1A` | Main background fill |
| Leather Light | `#5A3A2A` | Hover states, highlights on leather |
| Leather Dark | `#3A1A0A` | Inset shadows, slot interiors |
| Leather Red | `#6B3020` | Warm leather accent (alternate bg) |
| Blue Accent | `#2244AA` | Cloth ribbons, active tab highlight |
| Blue Dark | `#1A2855` | Inactive blue, cloth shadows |
| Cream Text | `#F5E6C8` | Body text, labels |
| Dark BG | `#1A0E08` | Darkest background areas, title bars |

### What NOT To Do
- **No flat solid-color rectangles** — everything needs depth (bevel, texture, or gradient)
- **No modern/minimal UI** — no rounded corners, no drop shadows, no glassmorphism
- **No bright/neon colors** — palette stays warm (golds, browns, deep blues)
- **No plain white or grey text** — use cream/gold tones instead
- **No untextured backgrounds** — leather should have grain, metal should have sheen

### Applying to Items & Blocks
- **Item textures**: 16x16 pixel art with warm lighting, subtle shading, and gold/metal highlights. Weapons/tools should look hand-forged. Items should match the medieval theme (no sci-fi, no modern)
- **Block textures**: Match vanilla Minecraft pixel density (16x16). Use warm wood tones, aged stone, and ornate metal. Decorative blocks should have carved/embossed details
- **Entity models**: Use Blockbench. Armor/clothing on entities should have visible rivets, leather straps, metal trim matching the gold palette

### Reference Files
- `Example images/example 1.png` — Panel frames with gold corners and blue ribbon accents
- `Example images/inventory and genral menu example .png` — Inventory layout with ornate gold frame, leather bg, blue cloth, medallions
- `Example images/numbers and text exmaple.png` — Gold 3D beveled font style
- `Resource Pack/` — Medieval resource pack for vanilla MC (dark variant, reference for general direction)

---

## Artifacts & Special Loot — IMPLEMENTED
- [x] **Relic & Artifact System** — 30 relics with full ability implementations, leveling, randomized stats, and upgradeable abilities. 10 RPG weapons with 20 combat skills.
  - Reference: [Relics Mod](https://modrinth.com/mod/relics-mod) (full source in `Ref Code/Relics/`)

### Core System (adapted from Relics mod)

#### Relic Leveling
- Each relic has independent level progression (0→10), gains XP through ability usage
- **Leveling Points** earned per level-up, spent to upgrade ability stats
- Stats scale via ADD (+flat), MULTIPLY_BASE (% per point), or MULTIPLY_TOTAL (compound growth)
- **Reroll**: Pay vanilla XP to re-randomize a stat's initial value
- **Reset**: Pay vanilla XP to reclaim spent leveling points
- **Exchange**: Convert vanilla XP to relic XP (cost increases per use)

#### Stat Randomization (MMO-style)
- On first pickup, each relic rolls randomized stats within min-max ranges
- Quality rating (0-10) based on how good the rolls are — hunt for perfect rolls
- Creates loot-chase gameplay: finding the same relic twice can give very different stats

#### Discovery & Loot
- Relics drop from **loot chests** in dungeons, temples, fortresses, etc. (~2.5% per relic type)
- Biome-specific loot pools determine which relics can drop where (see relic table below)
- Relics must be **researched** at a Researching Table before full info is revealed

#### Researching Table
- Craftable utility block (wood + lapis + gold ingots)
- Place a relic on it to research — reveals full ability descriptions, stat ranges, and lore
- Research progress is per-player (stored in capability data)
- Right-click: place/remove relic. Shift-click: open relic description GUI

### Relic List — Wearable Artifacts (26 items)

Relics equip in **accessory slots** (not regular armor). MegaMod adds: Back, Belt, Hands, Feet, Necklace, Ring (×2).

#### Back Slot (3)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Arrow Quiver** | *Receptacle*: Store 2-5 arrow stacks internally. *Leap* (Lv5): Launch upward + hover. *Agility* (Lv10): Faster bowstring draw. *Rain* (Lv15): Summon arrow rain at targeted location | Villages |
| **Elytra Booster** | *Acceleration*: Consume fuel items for elytra speed burst. Fuel capacity scales with upgrades | Mineshafts, Strongholds |
| **Midnight Robe** | *Vanish*: Invisibility + speed in low light. *Backstab*: Bonus damage from stealth, creates damage AOE circle | Mineshafts, Strongholds |

#### Belt Slot (3)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Leather Belt** | *Inventory*: Extra inventory row (3-6 slots). *Toughness* (Lv5): Small armor bonus | Villages |
| **Drowned Belt** | *Depths*: Increased underwater breathing + swim speed. *Pressure* (Lv5): Damage nearby mobs while submerged | Ocean Ruins, Shipwrecks |
| **Hunter Belt** | *Tracker*: Glowing on nearby mobs. *Harvest* (Lv5): Increased mob loot drops | Villages, Outposts |

#### Hands Slot (3, left/right)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Ender Hand** | *Neutrality*: Endermen don't aggro. *Swap*: Teleport to targeted entity (range scales 8-24 blocks) | End Cities |
| **Rage Glove** | *Fury*: Attack speed increase stacks with consecutive hits. *Brawl* (Lv5): Bare-hand damage scales with stacks | Villages |
| **Wool Mitten** | *Warmth*: Temperature resistance. *Comfort* (Lv5): Slow health regen when not in combat | Igloos, Frozen Biomes |

#### Feet Slot (6)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Magma Walker** | *Heat Resistance*: Immune to hot floor damage. *Pace*: Walk on lava (20-50s duration) | Nether Fortresses |
| **Aqua Walker** | *Walking*: Walk on water (30-60s duration). *Ripple* (Lv5): Damage nearby underwater mobs while walking | Ocean Monuments, Shipwrecks |
| **Ice Skates** | *Glide*: Massive speed boost on ice/packed ice. *Drift* (Lv5): Leave ice trail on water | Igloos, Frozen Biomes |
| **Ice Breaker** | *Shatter*: Stomp attack freezes nearby water + damages mobs. *Frost Armor* (Lv5): Thorns-like cold damage on hit | Frozen Biomes |
| **Roller Skates** | *Momentum*: Speed increases over time while moving on flat ground. *Leap* (Lv5): Double jump at max speed | Villages |
| **Amphibian Boot** | *Webbed*: Full swim speed + no water slowdown on land. *Leap* (Lv5): Frog-jump from water surface | Swamp, Mangrove |

#### Necklace Slot (3)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Reflection Necklace** | *Absorb*: Store 20-60 incoming damage. *Explode*: Release stored damage as AOE + stun (obsidian fragment VFX) | Nether Fortresses, Bastions |
| **Jellyfish Necklace** | *Unsinkable*: Can't sink in water. *Shock*: Electric damage on water contact. *Paralysis* (Lv5): Stun effect on shocked targets | Ocean, Shipwrecks |
| **Holy Locket** | *Steal*: Redirect nearby mob healing to the wearer. *Purify* (Lv5): Cleanse negative effects periodically | Villages, Temples |

#### Ring Slot (2, can wear 2 rings)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Bastion Ring** | *Alliance*: Piglins are neutral. *Compass*: Sense nearest bastion direction. *Trade* (Lv5): Better piglin barter rates | Bastion Remnants |
| **Chorus Inhibitor** | *Anchor*: Prevent enderman/shulker teleportation in radius. *Displace* (Lv5): Teleport hit mobs to random nearby location | End Cities |

#### Usable Items (no slot, held/thrown)

| Relic | Abilities | Loot Source |
|-------|-----------|-------------|
| **Shadow Glaive** | *Glaive*: Throw bouncing projectile (8 charges, 3-5 bounces). *Saw* (Lv5): Consume all charges → spinning saw blade | End Cities, Ancient Cities |
| **Infinity Ham** | *Regeneration*: Infinite food (restores 1-2 hunger per use). *Alchemy* (Lv5): Consume potions to add their effects to the ham | Villages |
| **Space Dissector** | *Dissection*: Create paired portals (16-32 block range, 5-10s lifetime). Walk through one, exit the other | Nether Fortresses |
| **Magic Mirror** | *Teleport*: Wormhole to spawn/bed (500-1000 block range, 60-120s cooldown) | Mineshafts, Strongholds |
| **Horse Flute** | *Paddock*: Store/release your horse. *Heal* (Lv5): Heal stored horse over time | Villages |
| **Spore Sack** | *Toss*: Throw tracking poison/debuff spores (applies Slowness + Poison). *Cloud* (Lv5): AOE poison cloud on impact | Lush Caves, Jungles |
| **Blazing Flask** | *Ignite*: Throw fire projectile. *Jetpack* (Lv5): Short burst of upward flight (consumes blaze powder fuel) | Nether Fortresses |

### RPG Weapons — Legendary Combat Items

Separate from relics. These are **mainhand weapons** with bound combat skills and VFX. Found from bosses or crafted at end-game.

| Rarity | Weapon | Slot | Skills |
|--------|--------|------|--------|
| Common | Lunar Crown | Head | **Spite** (Passive): Night vision + strength at night |
| Common | Solar Crown | Head | **Grace** (Passive): Speed + regen during day |
| Rare | Vampiric Tome | Offhand | **Blood Drinker** (Passive): 10% chance to drain health from nearby enemies |
| Rare | Static Seeker | Bow | **Spark Shot** (RMB): Arrow leaves Electron on hit. **Arc** (LMB): Lightning between you and Electron |
| Rare | Battledancer | Shield | **Pathfinder** (Passive): Speed trail for allies. **Basher** (F): Sprint through enemies |
| Epic | Ebonchill | Staff | **Glacial Quills** (LMB): 2-3 icicles, 15% slow. **Cryo Beam** (RMB): Ice beam, freezes you after 5s |
| Epic | Lightbinder | Gauntlet | **Convergence** (Passive): 30% block + stun chance. **Incinerator** (RMB): Charged fire beam |
| Legendary | Crescent Blade | Sword | **Moonlit Edge** (LMB): 3-hit combo, 3rd = lunar wave + knockup. **Lunar Rush** (RMB): Dash + slash, reusable within 2s |
| Legendary | Ghost Fang | Dagger | **Ghostblades** (LMB): Dash back + throw ghost blades. **Whispering Talon** (RMB): 4x zigzag dash combo, 50% poison |
| Legendary | Terra Warhammer | Hammer | **Earth Pound** (LMB): 5-hit ground smash, shockwave. **Earthquake** (RMB): Leap + ground smash + eruption |

### Implementation Status
- **FULLY IMPLEMENTED**: All 30 relics have working abilities with real game effects. Ability system with cast types (PASSIVE/INSTANTANEOUS/TOGGLE), cooldowns, stat scaling, HUD overlay (R/G keybinds), cross-system integration with Skills (Arcane boosts). 10 RPG weapons with 20 combat skills via RpgWeaponEvents. Skill tree integration: ABILITY_POWER boosts stats, COOLDOWN_REDUCTION reduces cooldowns, relic use grants Arcane XP.

### Design Notes
- **No Curios API dependency** — own accessory slot system (already implemented)
- Relic state stored in ItemStack NBT (level, XP, stat values, ability points)
- Ability activation via keybind (configurable, default: R for primary, G for secondary)
- Reuse textures, models, sounds, and entity renderers from `Ref Code/Relics/` — adapt package names
- Party/guild awareness: Damage/debuffs skip party members, heals/buffs only affect party
- Each RPG weapon skill needs VFX models (slash effects, impact particles, beams)
- **Economy integration**: Sell relics to MegaShop for MegaCoins based on quality rating

### Cross-System Integration (Relics ↔ Skills ↔ Dungeons)
- **Skill Tree → Relics**: Arcane tree Artificer/Mystic perks boost relic ability damage + cooldowns. Higher Arcane level = more attunement slots (equip more relics simultaneously). Minimum Arcane level may be required for Legendary relics
- **Dungeons → Relics**: Certain relics ONLY drop from dungeon bosses or dungeon treasure vaults (not world loot). Higher dungeon difficulty = better relic quality rolls. Boss-exclusive relics are the strongest in the game
- **Relics → Dungeons**: Equipped relics provide crucial advantages in dungeons (e.g., Midnight Robe stealth, Magma Walker lava immunity). Strategy layer: choose which relics to risk bringing into a permadeath dungeon
- **Relics → Skills**: Using relic abilities grants Arcane tree XP. Researching relics at the Researching Table grants Arcane XP

---

## Skill Trees — IMPLEMENTED
- [x] **Skill Tree System** — 5 trees, 25 branches, 100 nodes, 41 custom attributes, full persistence, GUI (K key), Computer app, XP triggers, cross-system integration
  - Reference: [Skills Mod](https://github.com/pufmat/skillsmod), [Attributes](https://modrinth.com/mod/attributes), [Pufferfish Additions](https://modrinth.com/mod/pufferfishs-unofficial-additions), [Default Skill Trees](https://modrinth.com/datapack/default-skill-trees)

### Custom Attributes (inspired by Pufferfish's Attributes)
40+ attributes across categories, applied as bonuses from skill nodes:

| Category | Attributes |
|----------|-----------|
| Damage | Melee, Ranged, Magic, Sword, Axe, Trident, Mace damage multipliers |
| Resistance | Melee, Ranged, Magic, general damage resistance |
| Speed | Mining speed, sprint speed, tool-specific speeds (pickaxe, axe, shovel), projectile speed (bow/crossbow) |
| Special | Stamina, Fortune, Life Steal, Healing, Jump height, Knockback, Stealth, Fall damage reduction, XP gain multiplier |
| Shred | Armor Shred, Toughness Shred, Protection Shred (penetrate enemy defenses) |

### Skill Trees

#### Combat Tree
Earn XP by killing mobs and dealing damage. Focused on fighting power.
- **Blade Mastery** — Increased sword/axe damage, attack speed, sweeping edge bonus
- **Marksman** — Ranged damage, projectile speed, draw speed for bows/crossbows
- **Juggernaut** — Melee/ranged resistance, knockback resistance, armor effectiveness
- **Berserker** — Life steal, damage boost at low HP, attack speed on kill chains
- **Slayer** — Bonus damage vs specific mob types (undead, arthropod, ender), rare drop chance

#### Mining Tree
Earn XP by breaking stone, ores, and deepslate. Focused on resource gathering.
- **Prospector** — Fortune bonus, ore detection (particle hint), increased rare ore chance
- **Efficiency Expert** — Mining speed, tool-specific speed boosts, reduced tool durability loss
- **Geologist** — XP bonus from ores, chance to double ore drops, deepslate mining speed
- **Excavator** — AOE mining (break adjacent blocks at high levels), faster shovel, fall reduction in caves
- **Spelunker** — Night vision in caves, reduced mob aggro range underground, lava resistance

#### Farming Tree
Earn XP by harvesting crops, breeding animals, fishing.
- **Green Thumb** — Crop growth speed in radius, bonus crop yield, auto-replant chance
- **Animal Husbandry** — Breeding cooldown reduction, tamed mob damage boost, extra breeding drops
- **Angler** — Fishing speed, treasure chance, fish variety bonus, XP from fishing
- **Herbalist** — Potion duration boost, extra potion yield from brewing, food saturation bonus
- **Beekeeper** — Honey yield, reduced bee aggro, pollination radius boost

#### Arcane Tree
Earn XP by enchanting, brewing, using XP, and using artifact items.
- **Enchanter** — Better enchantment rolls, reduced XP cost, chance to not consume lapis
- **Alchemist** — Longer potion effects, splash/lingering radius, reduced brewing time
- **Mystic** — Magic damage, magic resistance, mana-like cooldown reduction for artifact skills
- **Artificer** — Artifact skill damage bonus, reduced artifact cooldowns, unlock artifact attunement slots
- **Conduit** — XP gain multiplier, XP magnet range, reduced XP loss on death

#### Survival Tree
Earn XP by taking damage, eating food, traveling distance.
- **Ironhide** — Max health increase, natural armor, regeneration speed
- **Nomad** — Sprint speed, fall reduction, stamina (sprint duration), jump height
- **Scavenger** — Better loot from chests, mob drop quantity, salvage items for materials
- **Endurance** — Hunger drain reduction, food healing bonus, temperature resistance
- **Explorer** — Map reveal radius, compass utility, biome discovery XP bonus

### XP & Progression
- **XP Sources**: Killing mobs, mining blocks, harvesting crops, fishing, crafting, enchanting, brewing, taking damage, eating food, traveling distance
- **Anti-farming**: Per-chunk and per-entity XP limits to prevent grinding exploits (e.g., mob farms give diminishing returns)
- **Skill Points**: Earn 1 point per level-up in a tree. Spend on nodes. Trees level independently
- **Respec**: Pay MegaCoins at the computer's Shop app to reset a tree (cost scales with level)
- **Level Cap**: 50 per tree, ~15-20 nodes unlockable per tree (forces specialization, can't max everything)
- **Privileged Players**: "NeverNotch" and "Dev" bypass all restrictions silently — no level cap, no anti-farming limits, unlimited skill points. Checked the same way as `AdminSystem.java` (by username match, no UI indication to other players)

### Skill Tree GUI
- **Access**: `/megamod skills` command or via the Computer desktop (new "Skills" app icon) or hot key
- **Display**: Visual node graph per tree — unlocked nodes glow, available nodes pulse, locked nodes are dark
- **Node tooltips**: Show attribute bonuses, requirements, and connections to next nodes
- **Summary bar**: Shows current level, XP progress, and unspent points per tree

### Cross-System Integration

#### Economy Connection
- Respec costs MegaCoins (scaling: 100 * tree level)
- **Scavenger** perk increases MegaCoin drops from mobs
- **Prospector** perk increases MegaCoin drops from ores
- Stock Market: unlock the "Insider Trading" perk (Arcane tree) for a small stock price prediction hint

#### Artifact Connection
- Artifact items may require minimum Arcane tree level to equip (e.g., Legendary items need Arcane 25+)
- **Artificer** perk line directly boosts artifact skill damage and cooldowns
- **Mystic** perk line unlocks attunement — equip more artifacts simultaneously

#### Colony Connection
- Colony jobs require specific skill levels (e.g., Blacksmith job needs Mining 15+, Guard captain needs Combat 20+)
- Higher skill levels unlock better-paying colony jobs and quest tiers
- Colony war contributions grant bonus XP in Combat and Survival trees

---

## Roguelike Dungeons — IMPLEMENTED
- [x] **Roguelike Dungeon System** — 4 tiers, 5 themes, procedural room generation, 2 multi-phase bosses (Wraith, Ossukage), permadeath, randomly rolled loot (5 quality tiers), Dungeon Master NPC, quest progression, Soul Anchor, fog walls, boss bars
  - Reference: `Ref Code/DimDoors` (pocket dimension tech), `Ref Code/Dungeons & Bosses/Bosses-main` (boss entities, models, animations), `Ref Code/Dungeons & Bosses/TheAurorian-1.19.2` (dungeon gates, fog walls, boss spawners)

### Core Concept
Enter a portal to a procedurally generated dungeon in a pocket dimension. Choose your difficulty. Fight through randomized rooms. Defeat a multi-stage boss at the end. Earn rare loot with randomly rolled attributes. **No going back once you enter. Die and you lose all gear you brought.**

### Dungeon Entry
- **Dungeon Keys** — Tiered, one-time-use items obtained from a **Dungeon Master NPC** found in villages
  - Works like a DimDoors rift key — right-click to open a temporary portal to a dungeon pocket dimension
  - Portal appears, you step through, portal closes behind you. Key is consumed on use
  - **Key Tiers** (one per difficulty):
    - **Iron Key** (Normal) — Given freely by the Dungeon Master as an introductory quest
    - **Gold Key** (Hard) — Purchased with MegaCoins or earned by completing Normal dungeons
    - **Diamond Key** (Nightmare) — Rare reward from Hard dungeon bosses, or bought at high MegaCoin cost
    - **Netherite Key** (Infernal) — Crafted from Nightmare boss drops + rare materials, or extremely rare Dungeon Master stock
  - Keys are visually distinct (color/material matches tier) and show their difficulty tier in the tooltip
  - Keys are **not stackable** — each one is a commitment
- **Dungeon Master NPC** — Spawns in colonies, stationed at a small dungeon-themed stall/structure
  - Right-click to open a shop GUI with available keys, lore about dungeon themes, and difficulty warnings
  - Dialogue hints at what dungeon theme the key leads to (but layout is always random)
  - May offer quests: "Clear a Normal dungeon" → rewards a Gold Key, etc.
  - Ties into Colony System — colonies with a Dungeon Master attract more adventurer traffic
- **Difficulty Scaling** — Each key tier scales mob HP, damage, boss phases, loot quality
  - Normal: 2-phase boss, standard mobs, decent loot
  - Hard: 3-phase boss, elite mobs, rare+ loot guaranteed
  - Nightmare: 3-phase boss with minion waves, trap-heavy, epic+ loot possible
  - Infernal: 4-phase boss, mutated mobs, maximum danger, legendary loot possible
- **Point of No Return** — Once you step through the portal, it closes behind you. No /home, no ender pearls out, no escape
- **Permadeath Stakes** — If you die inside, you lose ALL gear you brought in (items are destroyed, not dropped). Incentivizes careful preparation and risk-reward decisions

### Procedural Generation
- **Room-based layout** — Dungeon built from a pool of pre-designed room templates stitched together procedurally
- **Room types**: Combat arenas, trap corridors, puzzle rooms, treasure vaults, rest shrines, mini-boss chambers
- **Branching paths** — Some dungeons have multiple routes, secret rooms, and dead ends
- **Biome theming** — Dungeon aesthetic matches a theme (Nether fortress, ice cavern, ancient temple, void castle, etc.)
- **Scaling** — Higher difficulty = more rooms, harder traps, denser mob spawns, more complex layouts

### Boss Encounters
- **Multi-stage bosses** — Every dungeon ends with a boss fight that has 2-4 phases
  - Phase transitions: boss changes attack patterns, summons minions, transforms, arena changes
  - Use boss models and animations from `Ref Code/Dungeons & Bosses/Bosses-main` (Wraith, Ossukage, Rat, Skeleton Minion)
  - Boss arena is a special room with fog walls (no escape until boss dies or you die)
- **Boss music** — Dynamic music that changes with boss phases (ref: `ClientBossMusicHandler.java`)
- **Boss spawner mechanic** — Altar/pedestal in arena triggers the fight (ref: `AncientAltarBlock.java`)

### Loot System
- **Randomly Rolled Attributes** — Dungeon loot items have randomized stat modifiers
  - Stats: damage, attack speed, durability, armor, special effects
  - Roll quality: Common → Uncommon → Rare → Epic → Legendary (color-coded)
  - Higher difficulty dungeons = higher minimum roll quality + more modifier slots
- **Special Features** — Rare items can roll unique abilities (life steal, chain lightning on hit, teleport strike, AOE explosion on kill, etc.)
- **Boss Drops** — Guaranteed rare+ item from the final boss. Higher difficulty = better guaranteed tier
- **Dungeon-exclusive items** — Some items/relics only drop from specific dungeon themes or boss types

### Cross-System Integration

#### Skill Tree Connection
- Dungeon difficulty tiers may require minimum skill levels to enter (e.g., Nightmare requires Combat 20+)
- Dungeon completion grants large XP bonuses to relevant skill trees
- **Spelunker** perks (Mining tree) reveal trap locations in dungeons
- **Slayer** perks (Combat tree) deal bonus damage to dungeon bosses

#### Economy Connection
- Dungeon keys purchasable from Dungeon Master NPC with MegaCoins (Gold Key ~500 MC, Diamond Key ~2000 MC, Netherite Key ~5000 MC)
- Dungeon loot can be sold at MegaShop for MegaCoins
- Higher-rolled items sell for more
- Pay MegaCoins for a "Soul Anchor" (keeps your gear on death, one-time use)

#### Colony Connection
- Dungeon Master NPC spawns in managed colonies — gives colonies more purpose
- Higher-tier colonies may stock better keys or offer key discounts
- Dungeon Master offers quests that chain: clear Normal → earn Gold Key → clear Hard → earn Diamond Key

#### Relic Connection
- Some relics only drop from dungeon bosses or dungeon treasure vaults
- **Artificer** perks improve rolled stats on dungeon loot

### Design Notes
- Pocket dimension tech from DimDoors ref — each dungeon instance is a separate pocket dimension
- Dungeon instances are temporary — destroyed after completion or death
- Room templates stored as structure NBT files, stitched together at generation time
- Mob spawns configured per-room with difficulty multipliers
- Boss entities need custom AI, animation state machines, and phase transition logic
- Consider party/co-op: multiple players can enter together, all share the stakes

---

## Colony System — IMPLEMENTED
- [x] **Colony Management** — Colony Hall claims territory, 9 building types, 9 citizen jobs with skill requirements, citizen AI (work/sleep/eat/idle), colony economy (daily income/expenses, treasury, 40+ trades), quest system (32 templates, quest board), war system (declaration scroll, pocket dimension battlefield, troop commands, victory spoils), defensive buildings (wall/tower/gate), research trees, building modules, raids, permissions, tax system, statistics

---

## Museum — IMPLEMENTED
- [x] **Museum Block** — Animal Crossing-inspired personal collection museum with physical pocket dimension building (5 wings), Curator NPC, Mob Net, donation system, display manager with ItemFrames/ArmorStands
  - **Crafting**: Nether star (center), gold blocks (sides), obsidian (top 3 + bottom 3)
  - **Placement**: Creates a player-specific museum instance — door becomes a gold portal transporting to a custom dimension (museum on a void island like the End)
  - **Wings**:
    1. **Aquarium** — Sea/water mobs in themed tanks
    2. **Wildlife Hall** — Land and air mobs in habitat dioramas
    3. **Hall of Achievements** — Wall plaques that light up as advancements complete
    4. **Art Gallery** — Famous paintings recreated as custom Minecraft pixel art
    5. **Hall of Items** — Complete item encyclopedia; donated items on pedestals, missing shown as grey silhouettes
  - **Museum Curator NPC** — Sits at entrance, right-click to interact
    - Collection UI showing progress per wing (e.g., "Aquarium: 12/43") with overall completion tracker
    - Each wing tab lists every entry — donated in full color, missing greyed out
    - **Single Donate**: Hand curator an item/mob egg/art piece to donate
    - **Quick Donate**: Button scans inventory, donates all new items at once with summary popup
    - Curator has idle dialogue and reacts to milestones
- [x] **Mob Net** — Item for capturing mobs (right-click)
  - Swing at any mob to attempt capture
  - Successful capture produces a mob egg matching the mob type
  - Eggs can be donated to museum or placed to release the mob
  - Craftable (string + slimeball + sticks or similar)
