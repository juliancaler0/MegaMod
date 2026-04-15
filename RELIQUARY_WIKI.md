# MegaMod Reliquary Wiki

## Overview

The Reliquary port bundles the full Reliquary mod (P3pp3rF1y's magic/RPG mod) into MegaMod under the `reliquary` namespace. The port targets Minecraft 1.21.11 / NeoForge 21.11 and covers every feature shipped in the upstream 1.21.x source: the full relic catalogue, handgun + bullet/magazine system, custom potion brewing, Alkahestry, pedestals, mob charms, and the fluid XP bucket.

Every ID stays under `reliquary:` so copied datapacks and resource packs from upstream resolve without rewrites.

---

## Blocks

| Block | Function |
|-------|----------|
| **Altar of Light** | Converts redstone into glowstone. Right-click with redstone dust or a charged Tome of Alkahestry to activate. Must be under open sky and daytime to grow a glowstone block on top. |
| **Lilypad of Fertility** | Speeds up growth of crops nearby. Stacks when multiple are placed adjacently. |
| **Interdiction Torch** | Pushes all non-boss mobs and projectiles within its range away. Places on floor or wall. |
| **Pedestal** | Displays an item cosmetically. When redstone-powered, activates item abilities (see "Pedestal Interactions" below). 16 dye variants. |
| **Passive Pedestal** | Display-only variant of the Pedestal — items float and rotate but never activate. 16 dye variants. |
| **Wraith Node** | Teleport destination for Ender Staff in "Wraith Node" mode. Right-click it with the staff to bind; right-click again with an activated staff to return. |
| **Apothecary Mortar** | 3-slot grinder used to make potion essences. See "Potion System" below. |
| **Apothecary Cauldron** | Brews potion essences into finished potions. Requires a heat source below + water inside. |

### Pedestal Interactions

Pedestals only fire item abilities when powered by redstone — a powered pedestal block or a redstone-dust pedestal within 10 blocks. Supported items:

- **Coin of Fortune** — vacuums items and XP orbs into attached chests/tanks.
- **Hero's Medallion** — stores XP from nearby XP orbs; acts as an XP tank.
- **Sword / tinker's cleaver / etc.** — swings at nearby mobs.
- **Bucket** — milks adjacent cows or drains adjacent water/lava source blocks into attached tanks.
- **Shears** — shears nearby sheep or shearable blocks.
- **Rending Gale (flight mode)** — grants localized creative flight, drains charge per tick.
- **Rending Gale (push/pull/bolt)** — push/pull mobs or call lightning (same rules as handheld, no charge cost).
- **Harvest Rod** — tills/plants/harvests an automated farm in range.
- **Fishing rod / Rod of Lyssa** — casts a line; pedestal renders the hook.

---

## Chargeable & Toggleable Items

Shift-right-click any of these to toggle on/off. An enabled item shows the enchantment glint. Most of them **drain their ammo/fuel from the player's inventory** automatically while enabled.

| Item | Charges on | Modes |
|------|------------|-------|
| **Tome of Alkahestry** | Redstone dust/block, Glowstone dust/block | Off / On |
| **Destruction Catalyst** | Gunpowder | Off / On — right-click to destroy mundane blocks in a small radius (ores + valuable blocks spared) |
| **Emperor's Chalice** | Water source blocks | Drain / Place — infinite water bucket + empty bucket. Also feeds Apothecary Cauldron / Botania Apothecary / TC Crucible. Right-click held to refill hunger for a health trade. |
| **Ender Staff** | Ender pearls | Pearl / Ender eye / Wraith Node — safer throw than vanilla pearls; long-cast doubles range; node mode teleports to the bound Wraith Node. |
| **Coin of Fortune** | — | Magnet on / off — pulls items + XP within radius. Hold right-click for a larger radius. |
| **Glacial Staff** | Snowballs | Off / On — ranged snowball gun; freezes water → packed ice and lava → obsidian under player. |
| **Ice Magus Rod** | Snowballs | Off / On — ranged snowball weapon (no freezing). |
| **Harvest Rod** | Bone meal + plantables | Bone meal / Seeds / Hoe — till, plant, fertilise in area; breaking a crop breaks nearby crops too. |
| **Hero's Medallion** | Experience | Off / On — stores XP. Off-mode right-click withdraws one level per click. |
| **Infernal Chalice** | Lava | Drain / Place — infinite lava bucket + empty bucket. Feeds TC smeltery and other tank blocks. |
| **Infernal Tear** | Auto-selected item | Off / On — on first activation scans inventory for the most common accepted item; drains that item into XP thereafter. |
| **Lantern of Paranoia** | Torches (via Sojourner's Staff or inventory) | Off / On — auto-places torches in dark spots around the player. |
| **Touchstone of Midas** | Glowstone dust/block | Off / On — repairs gold-based items in inventory over time (Cross of Mercy, Magic Bane, gold armour, etc.). |
| **Pyromancer's Staff** | Fire charges + blaze powder | Blaze / Fire charge / Erupt / Flint-and-steel — fireballs, AOE burn, or ignite. Destroys incoming fireballs and extinguishes fires when off. |
| **Rending Gale** | Feathers | Flight / Pull / Push / Bolt — localized flight, push/pull mobs, call lightning (rain required for bolt). |
| **Sojourner's Staff** | Torches | Off / On — places torches at range for a small torch cost. |
| **Twilight Cloak** | — | Off / On — mobs ignore the player at night even if hit. |
| **Void Tear** | Auto-selected item | Off / On / Modes — portable mass storage for one item/block type. See "Void Tear Modes" below. |

### Void Tear Modes

- **Full Inventory** — keeps filling the inventory from the tear.
- **No Refill** — drains into the tear; never feeds back out.
- **One Stack** — keeps exactly one stack of the selected item in inventory.

---

## Other Items

| Item | Function |
|------|----------|
| **Angelheart Vial** | Resurrection vial. Shatters when you'd die, restores health + grants brief buffs. |
| **Angelic Feather** | Negates fall damage by draining hunger instead. |
| **Phoenix Down** | Upgraded Angelic Feather: also prevents death (like Angelheart Vial). |
| **Aphrodite's Serum** | Thrown at animals → puts them in love mode. |
| **Fertile Potion** | Thrown at crops → triggers growth. |
| **Glowing Bread** | Best food in game — fills hunger + saturation fully. |
| **Glowing Water** | Splash potion that heavily damages undead, harmless to everything else. |
| **Holy Hand Grenade** | Powerful thrown explosive; one-shots most mobs. |
| **Infernal Claws** | Fire immunity at hunger cost. |
| **Kraken Shell** | Drowning immunity + night vision + haste + speed underwater (hunger cost). |
| **Magic Bane** | Sword that can random-debuff struck mobs; scales with enchantment count. |
| **Cross of Mercy** | Sword that deals bonus damage to undead. |
| **Rod of Lyssa** | Fishing rod that also fishes dropped items, pulls entities (strong vertical), and steals mob inventory items when sneaking. |
| **Salamander's Eye** | Extinguishes fires around player + reflects ghast fireballs. |
| **Serpent Staff** | Shoots slime balls. |
| **Shears of Winter** | Left-click = regular shears; held right-click = AOE shears on sheep, or AOE breaker on plants/leaves. |
| **Witch Hat** | Wearable hat (head equipment slot). Used as a crafting ingredient. |
| **Witherless Rose** | In inventory → immune to the wither effect. |

---

## Mob Charms and Fragments

Killing a specific mob with the **Severing** enchantment on your weapon drops a **Mob Charm Fragment**. Combine 4 matching fragments into a **Mob Charm**. Any charm (in inventory, belt, or nearby pedestal) prevents the matching mob from targeting or hitting you.

- **Mob Charm Belt** — holds all charms. Right-click to open its GUI. Compatible with Curios/Accessories belt slot.
- **Severing enchantment** — boosts mob-drop rate including fragments.

---

## Mob Drops / Crafting Ingredients

Reliquary-specific drops (injected into vanilla mobs via loot modifiers):

- **Bone Rib** — Skeletons
- **Wither Rib** — Wither Skeletons
- **Chelicerae** — Spiders
- **Creeper Gland** (Catalyzing Gland) — Creepers + Ghasts
- **Slime Pearl** — Slimes + Magma Cubes
- **Bat Wing** — Bats
- **Zombie Heart** — Zombies + Zombified Piglins
- **Molten Core** — Magma Cubes
- **Eye of the Storm** — Charged Creepers
- **Frozen Core** — Snow Golems
- **Nebulous Heart** — Endermen
- **Squid Beak** — Squids
- **Guardian Spike** — Guardians

Crafting components: **Fertile Essence, Infernal Claw, Kraken Shell Fragment, Crimson Cloth, Zombie Pearl, Skeleton Pearl, Wither Skeleton Pearl, Creeper Pearl**.

---

## Tome of Alkahestry

### Charging

1. Shift-right-click the tome to toggle **on** (enchantment glint appears).
2. Carry **redstone dust/block** or **glowstone dust/block**. Every ½ second the tome absorbs up to 16 items and converts them to charge (1 redstone = 1 charge; redstone block = 9; glowstone dust = 1; glowstone block = 4).
3. Tome max charge is configurable (default 1000).

### Duplicating items (normal players)

Place the tome + a supported item/block in a crafting table → the recipe produces `result_count` of the item for `charge` cost (charge is deducted from the tome; the tome stays in the grid with reduced charge).

Default duplication list (from datapack):

| Item | Count | Charge |
|------|-------|--------|
| Dirt / Cobblestone / Sand | 32 | 4 |
| Gravel | 16 | 4 |
| Sandstone | 8 | 4 |
| Clay | 2 | 4 |
| Netherrack / Charcoal | 8 / 4 | 4 |
| Lapis Lazuli | 1 | 4 |
| Obsidian | 4 | 8 |
| Soul Sand | 8 | 8 |
| Coal | 4 | 8 |
| Gunpowder | 2 | 8 |
| Flint | 8 | 8 |
| Gold / Iron / Emerald / Tin / Silver / Copper / Steel Ingot | 1 | 32 |
| Diamond | 1 | 64 |
| Nether Star | 1 | 256 |

### Duplicating items (admins)

Admin-only fallback path (only admins as defined by `AdminSystem.isAdmin`):

1. Hold the tome in one hand, the target item in the **other hand**.
2. Right-click with the tome → duplicates **a full stack** of the off-hand item.
3. Shift + right-click → duplicates **one** of the off-hand item.
4. Each action costs a flat **256 charge** regardless of item (matches the Nether Star ceiling). If the tome lacks 256 charge the action is refused with a shortfall message.
5. Works on literally any item or block, not just the default duplication list.

---

## Handgun

Reliquary's handgun fires **shots** from loaded **magazines**. The handgun is a single-shot ranged weapon with a reload animation.

### Loading

1. Craft an **Empty Magazine** (iron + glass + stone).
2. Combine Empty Magazine + 8 bullets of a single type → a loaded magazine.
3. Hold the handgun, right-click — if ammo is loaded it fires; if not and a filled magazine is in inventory, it reloads (spawns Empty Magazine back).

### Shot Types

| Shot | Effect |
|------|--------|
| **Neutral** | Base kinetic damage. |
| **Exorcism** | Bonus damage vs undead. |
| **Blaze** | Fire damage — strong vs most mobs, useless against fire-immune. |
| **Seeker** | Seeks nearest mob — lenient aim. |
| **Ender** | Seeks and pierces multiple mobs. |
| **Concussive** | Small non-destructive explosion on impact. |
| **Buster** | Large destructive explosion. |
| **Sand** | Blinds target; triggers creeper explosion. |
| **Storm** | Stronger when raining, strongest in thunderstorms; charges creepers (→ Eye of the Storm drop). |

All shots can additionally carry a **potion effect** (craft the loaded magazine around a splash/lingering potion to brew a potion-imbued magazine).

---

## Potion System

Reliquary's potions are brewed independently of vanilla. The flow is **Apothecary Mortar → Apothecary Cauldron → Empty Potion Vial**.

### Apothecary Mortar

Combine **2–3 potion ingredients** (including optionally one previous Potion Essence) in the mortar. Right-click to grind. The result is a **Potion Essence** whose effects are the intersection of the ingredients' passive effects. Duration + potency are summed with a small loss — you cannot infinitely re-grind for forever-long potions.

### Apothecary Cauldron

1. Place a heat source below (fire/lava/campfire).
2. Fill with water.
3. Drop in the Potion Essence.
4. Drop in nether wart to finalize. Particles start.
5. (Optional) Drop in gunpowder before nether wart → splash potions.
6. (Optional) Drop in up to 4 **glowstone** → potency boost (each extra has diminishing return).
7. (Optional) Drop in up to 5 **redstone** → duration boost.
8. Extract 3 potions with Empty Potion Vials.

### Custom Effects

- **Cure** — cures zombie villagers in range.
- **Flight** — grants creative flight while active.
- **Pacification** — affected mobs cannot attack the player.

### Ingredient Passive Effects

See the upstream wiki for the full ingredient→effects table (~70 ingredients across 4 tiers). The port maintains bit-for-bit parity with `PotionMap.initPotionMap()` from the reference.

---

## XP Bucket

The **Bucket of Experience** is a regular bucket item that stores Reliquary's custom XP fluid (`reliquary:xp_still`). Fillable/emptyable at any fluid source/sink that supports the XP fluid. The bucket icon is rendered via NeoForge's `neoforge:fluid_container` ItemModel against the XP fluid's still sprite, so it tints itself automatically.

---

## Port Notes (1.21.11)

Things that changed vs. upstream 1.21.1/1.21.x and how the port handles them:

- **Ingredient codec** — `Ingredient.CODEC` is now `Codec.xor(customIngredientCodec, nonEmptyHolderSetCodec)`. Neither branch accepts the legacy `{"item": "minecraft:redstone"}` object form, so every Alkahestry recipe uses the compact form `"minecraft:redstone"` / `"#c:gems/diamond"`.
- **Fluid container model** — `neoforge:fluid_container` moved from a model-loader to an `ItemModel` type registered via `ClientNeoForgeMod.registerItemModels`. The XP bucket now declares `model.type: neoforge:fluid_container` in `items/xp_bucket.json` directly, instead of `models/item/xp_bucket.json` with `loader`.
- **Block entity sync** — `BlockEntityBase.getUpdateTag(HolderLookup.Provider)` is overridden via `TagValueOutput` so pedestals/mortars/altars propagate their item data on initial chunk load (not just on updates).
- **Equipment assets** — Witch Hat uses the 1.21.11 Equippable data component with `assets/reliquary/equipment/witch_hat.json` and the new texture path `textures/entity/equipment/humanoid/witch_hat.png`.
- **`forge:` tags** — all legacy `forge:ingots/*` references updated to `c:ingots/*`.
- **Select-item models** — empty-vs-loaded variants for Void Tear / Handgun / Lyssa Rod / Infernal Tear are driven by `assets/reliquary/items/<name>.json` select-item JSON instead of the old `ItemProperties.register` Java predicates.

---

## Admin / Feature Toggles

The port consults `FeatureToggleManager` (accessible via the Computer's admin Toggles tab). Each subsystem can be disabled individually:

- `reliquary` (master switch)
- `reliquary_handgun`
- `reliquary_pedestals`
- `reliquary_alkahestry`
- `reliquary_apothecary`
- `reliquary_potions_replace_alchemy`
- `reliquary_relics`
- `reliquary_mob_charms`
- `reliquary_void_tear`
- `reliquary_fragment_drops`
- `reliquary_witherless_rose`
- `reliquary_chest_loot`

Disabled subsystems silently no-op; the admin toggle does not remove already-placed blocks or items.

---

## Compatibility

The port ships compat modules for:

- **Accessories** (our in-house accessory slot system — Witch Hat in head slot, Mob Charm Belt in belt slot, etc.)
- **Curios** (upstream accessory mod — same slots, fallback if Accessories is disabled)
- **Botania** (Petal Apothecary accepts Emperor's Chalice)
- **Jade** (tooltip overlay for pedestal contents, cauldron brewing progress)
- **JEI** (every Reliquary recipe and custom recipe type has a category)
- **Tinker's Construct** (Infernal Chalice ↔ smeltery tank fluid transfer)
