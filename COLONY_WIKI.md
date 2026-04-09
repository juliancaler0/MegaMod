# MegaMod Colony System Wiki

## Overview

MegaMod's Colony System is a comprehensive town-building system inspired by MineColonies, fully integrated into the MegaMod experience. Create thriving colonies with NPC workers, manage buildings, research technologies, defend against raiders, and craft decorative blocks — all while using MegaMod's economy, computer interface, and faction systems.

---

## Getting Started

### Founding a Colony

1. **Craft a Supply Camp or Supply Chest** from the Colony Items creative tab
2. **Place it** in the world — this creates your initial colony structure
3. **Place a Town Hall hut block** to establish the colony center
4. **Create a faction** via the Computer's Town app if you don't have one already
5. Your faction IS your colony — all colony features are managed through your faction

### The Computer / Phone Interface

Access colony management through the **Computer block** or **Phone item**. Open the **Town** app to see 12 management tabs:

| Tab | Purpose |
|-----|---------|
| Overview | Colony summary: citizen count, happiness, buildings, economy |
| Workers | Manage all worker citizens, view jobs and skills |
| Recruits | Manage combat recruits, formations, equipment |
| Groups | Organize recruits into squads with formations |
| Diplomacy | Create alliances, declare wars, manage treaties |
| Territory | Claim/unclaim chunks, view borders |
| Upkeep | Daily costs, food stockpile, economic health |
| War | Active sieges, raid history, defense status |
| Work Orders | Builder assignments, construction progress |
| Buildings | All placed buildings with levels, upgrade buttons |
| Research | University tech tree, start/track research |
| Requests | Active item requests, manual fulfillment |

---

## Systems

### Colony Border System

Colony borders are defined by **claimed chunks**. Each faction can claim up to 50 chunks.

- Borders render as colored lines at chunk boundaries (gold = yours, green = allied, red = enemy)
- Hold Ctrl to see chunk ticket boundaries
- Claim chunks via the Territory tab or `/colony claim` command
- Borders extend full world height

### Colony Protection System

Claimed territory is protected from unauthorized modification:

- **Block Protection**: Non-members cannot break or place blocks
- **Interaction Protection**: Non-members cannot open containers or use blocks
- **Explosion Protection**: Configurable — DAMAGE_NOTHING / DAMAGE_PLAYERS / DAMAGE_ENTITIES / DAMAGE_EVERYTHING
- **Citizen Protection**: Non-members cannot damage your citizens
- **Creative/Admin Bypass**: Creative mode players and admins bypass all protection

### Command System

**Colony Commands** (`/colony`):
| Command | Permission | Description |
|---------|-----------|-------------|
| `/colony info` | All | Show your colony stats |
| `/colony list` | All | List all colonies on server |
| `/colony teleport <id>` | All | Teleport to a colony center |
| `/colony raid <culture>` | Admin | Force start a raid |
| `/colony backup` | Admin | Save colony data backup |

**Citizen Commands** (`/citizen`):
| Command | Permission | Description |
|---------|-----------|-------------|
| `/citizen list` | All | List citizens in your colony |
| `/citizen info <name>` | All | Show citizen details |
| `/citizen spawn` | Admin | Spawn a new citizen |
| `/citizen kill <name>` | Admin | Remove a citizen |

### Happiness and Saturation Systems

Citizens have a happiness score (0-10) that affects work speed (0.5x to 1.5x multiplier).

**Static Modifiers:**
| Modifier | Weight | How It Works |
|----------|--------|-------------|
| School | 1.0 | Requires a School building at level 1+ |
| Mystical Site | 1.0 | Higher mystical site level = more happiness |
| Security | 4.0 | More guards relative to workers = happier |
| Social | 2.0 | Fewer unemployed/homeless/sick/hungry = happier |
| Food | 3.0 | Food diversity and quality based on home level |

**Time-Based Modifiers** (degrade if needs unmet):
| Modifier | Weight | What Causes It |
|----------|--------|---------------|
| Homelessness | 3.0 | No assigned residence |
| Unemployment | 2.0 | No assigned job |
| Health/Disease | 2.0 | Untreated sickness |
| Idle at Job | 1.0 | No work available |
| Sleep | 1.5 | Disrupted sleep cycle |

**Formula**: `happiness = sum(factor x weight) / totalWeight x (1 + researchBonus) x 10`, capped at 10.

### Pathing System

Citizens use custom pathfinding that respects:
- **Waypoint blocks** — placed in blueprints to guide pathfinding
- **Doors and fence gates** — citizens can open them
- **Water** — citizens can swim (float goal)
- Custom node evaluation for colony-specific blocks

### Raid System

Colonies are attacked by 5 raider cultures, each with 3 unit tiers:

| Culture | Melee | Ranged | Leader | Biome |
|---------|-------|--------|--------|-------|
| Barbarian | Swordsman | Archer | Chief | Plains, Forest |
| Pirate | Cutlass | Crossbow | Captain | Ocean, Beach |
| Egyptian | Mummy | Archer | Pharaoh | Desert |
| Norsemen | Shieldmaiden | Archer | Chief | Taiga, Snowy |
| Amazon | Spearman | Archer | Chief | Jungle |
| Drowned Pirate | Trident | Bow | Captain | Deep Ocean |

**Difficulty**: Scales 1-14 (starts at 7). Citizen deaths reduce difficulty; successful defenses increase it.
**Scheduling**: Raids occur after a configurable number of nights. Multiple waves spawn during a raid.
**Rewards**: MegaCoins + Mastery Marks for perfect defense (no citizen deaths).

### Request System

Buildings automatically request items they need. The system uses a priority-based resolver chain:

1. **Warehouse Resolver** (priority 50) — checks warehouse stock
2. **Crafting Resolver** (priority 75) — assigns to a crafting building
3. **Deliveryman Resolver** (priority 100) — assigns physical delivery
4. **Player Resolver** (priority 200) — fallback, player fulfills manually

View and manually fulfill requests in the **Requests** tab of the Town screen.

### Research System

Research is conducted at the **University** building by Researcher citizens.

**Three Branches:**
- **Civilian** — worker efficiency, food quality, happiness bonuses
- **Technology** — building upgrades, recipe unlocks, crafting speed
- **Combat** — guard strength, raid defense, armor improvements

Each research has:
- **Costs** — items or XP levels
- **Requirements** — building levels, parent research completed
- **Effects** — stat multipliers, feature unlocks, flat modifiers

Manage research in the **Research** tab of the Town screen.

### Sleep System

- Citizens sleep in beds assigned in their **Residence** building
- Sleep occurs at night (Minecraft day/night cycle)
- Sleeping restores health and resets food saturation
- Disrupted sleep reduces happiness (Sleep modifier, weight 1.5)

### Worker System

Each worker has:
- **Primary & Secondary Skills** — from 11 possible skills
- **XP gain** — based on work actions, modified by building level and intelligence
- **Skill distribution** — Primary: 100%, Complimentary: +10%, Adverse: -10%, Secondary: 50%
- **Level cap** — based on home building level (max 100)

**11 Citizen Skills:**
| Skill | Complimentary | Adverse |
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

### Disease System

- **Base chance**: Random per-tick disease check
- **Spread**: 1% chance on collision with sick citizen
- **Food diversity**: Reduces susceptibility — more varied diet = healthier
- **Immunity**: 90 minutes after cure (900 min with vaccine research)
- **Healers immune**: Hospital workers cannot get sick
- **Treatment**: Healer citizens at the Hospital building cure diseases

---

## Dependencies (Integrated)

### Structurize (Blueprint System)

The blueprint system handles all schematic operations:

**Tools:**
| Tool | Purpose |
|------|---------|
| Scan Tool | Mark two corners, scan a world region into a .blueprint file |
| Build Tool | Browse style packs, place blueprints with rotation/mirror preview |
| Shape Tool | Generate procedural shapes (cube, sphere, pyramid, cylinder, etc.) |
| Tag Tool | Tag positions within blueprints for special behavior |

**26 Style Packs** with 9,926+ blueprints:
Acacia, Ancient Athens, Birch, Caledonia, Cavern, Colonial, Dark Oak, Fortress, Incan, Jungle, Lost City, Medieval Birch/Oak/Spruce/Dark Oak, Nordic, Original, Pagoda, Sandstone, Shire, Space Wars, Stalactite Caves, Warped Netherlands

### Domum Ornamentum (Decorative Blocks)

Craft combinatorial decorative blocks at the **Architects Cutter**:

- Place up to 3 material blocks in the input slots
- Browse available decorative block outputs
- Each output uses the textures of your chosen materials

**20 Block Types:**
Timber Frames (10 variants), Framed Lights (6), Shingles (5 heights), Shingle Slabs, Pillars (3 shapes), Panels, Posts, Paper Walls (2), Doors, Fancy Doors, Trapdoors, Fancy Trapdoors, Extra Blocks (27 variants), Barrels (2), All-Brick Blocks, Fences, Fence Gates, Slabs, Walls, Stairs

**16 Floating Carpets** — one per dye color, can be placed in mid-air

### Multi-Piston

A configurable block mover:
- Right-click to open GUI
- Set input direction (where blocks come from) and output direction (where they go)
- Set range (1-10 blocks) and speed (1-3)
- Activated by redstone signal
- Signal ON = push toward output; Signal OFF = pull from input

---

## Blocks & Items

### Tools & Scepters
| Item | Purpose |
|------|---------|
| Scan Tool | Scan world regions into blueprints |
| Build Tool | Place blueprints from style packs |
| Shape Tool | Generate procedural shapes |
| Tag Tool | Tag blueprint positions |
| Build Goggles | Visualize build previews |
| Scan Analyzer | Analyze scanned structures |
| Clipboard | View colony work orders and pending requests |
| Lumberjack Scepter | Manage tree cutting areas |
| Guard Scepter | Command guard positions |
| Permission Scepter | Manage colony permissions |
| Beekeeper Scepter | Manage bee areas |

### Scrolls
| Scroll | Effect |
|--------|--------|
| Colony Teleport | Teleport to your colony center |
| Area Teleport | Teleport to a specific area |
| Buff Scroll | Apply temporary buffs |
| Guard Help | Call guards to your location |
| Highlight Scroll | Highlight blocks in an area |
| Resource Scroll | View builder material requirements |

### Supply Items
| Item | Purpose |
|------|---------|
| Supply Camp | Place to found a new colony (land) |
| Supply Chest | Place to found a new colony (compact) |

### Weapons
| Weapon | Stats |
|--------|-------|
| Chief Sword | High damage, raider chief weapon |
| Iron Scimitar | Fast attack speed |
| Pharaoh Scepter | Magic damage |
| Colony Spear | Throwable melee weapon |
| Fire Arrow | Incendiary projectile |

### Armor
| Set | Pieces | Defense Level |
|-----|--------|--------------|
| Plate Armor | Helmet, Chest, Legs, Boots | Between Iron and Diamond |
| Pirate Captain | Hat, Top, Leggings, Boots | Leather tier |
| Pirate Crew | Cap, Chest, Legs, Shoes | Leather tier |
| Santa Hat | Head only | Leather tier |

### Utility Blocks
| Block | Purpose |
|-------|---------|
| Rack | Storage block for warehouse system (27 slots) |
| Stash | Personal colony storage |
| Barrel | Compost barrel — ferments items into compost (20 minutes) |
| Scarecrow | Farm field marker — assigns fields to farmers |
| Plantation Field | Plantation crop area marker |
| Construction Tape | Visual build zone boundary marker |
| Waypoint | Pathfinding navigation point for citizens |
| Decoration Controller | Blueprint anchor for decoration placement |
| Colony Banner | Faction-colored banner block |
| Colony Sign | Colony name signage |
| Iron Gate | Sturdy gate (hardness 10) |
| Wooden Gate | Standard gate (hardness 7) |
| Postbox | Player interface for the request system |

### Sifter Meshes
| Mesh | Durability | Drops |
|------|-----------|-------|
| String | 500 | Basic materials (flint, clay, seeds) |
| Flint | 1,000 | Low-tier ores (iron/gold nuggets, coal) |
| Iron | 1,500 | Mid-tier ores (raw iron/copper, emerald, lapis) |
| Diamond | 2,000 | High-tier (diamond, emerald, quartz, prismarine) |

### Assistant Hammers
| Hammer | Durability |
|--------|-----------|
| Gold | 200 |
| Iron | 400 |
| Diamond | 1,000 |

### Miscellaneous Items
| Item | Purpose |
|------|---------|
| Ancient Tome | Lore/enchantment book |
| Compost | Fertilizer for colony farmland |
| Mistletoe | Holiday decoration |
| Magic Potion | Buff potion |
| Colony Map | View colony layout |
| Quest Log | Track active quests |
| Rally Banner | Rally guards to a location |

---

## Buildings

### Resource Production (7)
| Building | Worker | What It Produces |
|----------|--------|-----------------|
| Farmer's Hut | Farmer | Crops, seeds |
| Mine | Miner | Ores, stone (depth tiers: 48/16/-16/-100) |
| Forester's Hut | Forester (Lumberjack) | Logs, wood |
| Fisher's Hut | Fisher | Fish, fishing loot |
| Plantation | Planter | Bamboo, cactus, kelp, sugar cane, vines, etc. |
| Nether Mine | Nether Miner | Nether resources (quartz, gold, ancient debris) |
| Quarry | Quarrier | Stone, ore (systematic layer mining) |

### Animal Husbandry (6)
| Building | Worker | Animals |
|----------|--------|---------|
| Shepherd's Hut | Shepherd | Sheep (wool, mutton) |
| Cowhand's Hut | Cowhand | Cattle (leather, beef) |
| Chicken Farmer's Hut | Chicken Farmer | Chickens (eggs, feathers) |
| Swineherd's Hut | Swineherd | Pigs (pork) |
| Rabbit Hutch | Rabbit Herder | Rabbits (hide, meat) |
| Apiary | Beekeeper | Bees (honey, honeycomb) |

### Crafting & Production (19)
| Building | Worker | Primary/Secondary Skill | Products |
|----------|--------|------------------------|----------|
| Bakery | Baker | Knowledge/Dexterity | Bread, pastries, doughs |
| Blacksmith's Hut | Blacksmith | Strength/Focus | Tools, weapons, armor |
| Stonemason's Hut | Stonemason | Creativity/Dexterity | Stone blocks, decorative stone |
| Sawmill | Carpenter | Knowledge/Dexterity | Planks, wooden items |
| Smeltery | Smelter | Athletics/Strength | Smelted metals |
| Brick Yard | Stone Smelter | Athletics/Dexterity | Smelted stone, bricks |
| Crusher's Hut | Crusher | Stamina/Strength | Gravel, sand, flint |
| Sifter's Hut | Sifter | Focus/Strength | Sifted ores (mesh-based) |
| Chef's Kitchen | Chef | Creativity/Knowledge | Advanced meals (Tier 2-3) |
| Dining Hall | Cook | Adaptability/Knowledge | Basic cooked food (Tier 1) |
| Dyer's Hut | Dyer | Creativity/Dexterity | Dyed blocks and items |
| Fletcher's Hut | Fletcher | Dexterity/Creativity | Arrows, bows |
| Flowershop | Florist | Dexterity/Agility | Flowers, arrangements |
| Glassblower's Hut | Glassblower | Creativity/Focus | Glass items |
| Concrete Mixer's Hut | Concrete Mixer | Stamina/Dexterity | Concrete blocks |
| Composter's Hut | Composter | Stamina/Athletics | Compost, bone meal |
| Mechanic's Hut | Mechanic | Knowledge/Agility | Redstone items, mechanisms |
| Alchemist Laboratory | Alchemist | Dexterity/Mana | Potions |
| Enchanter's Tower | Enchanter | Mana/Knowledge | Enchanted items |

### Military (6)
| Building | Worker | Purpose |
|----------|--------|---------|
| Guard Tower | Knight | Defense post, patrol area |
| Barracks | — | Military housing (contains towers) |
| Barracks Tower | Knight | Guard tower within barracks |
| Archery | Archer Training | Ranged combat training |
| Combat Academy | Combat Training | Melee combat training |
| Gate House | — | Colony entrance control |

### Education (3)
| Building | Workers | Purpose |
|----------|---------|---------|
| Library | Pupil | Basic study, skill progression |
| School | Teacher + Pupil | Educates children, intelligence growth |
| University | Researcher | Conducts research, advances tech tree |

### Services (5)
| Building | Worker | Purpose |
|----------|--------|---------|
| Hospital | Healer (Doctor) | Cures diseases, heals citizens |
| Graveyard | Undertaker | Manages graves, burial ceremonies |
| Tavern | — | Visitors spawn here for recruitment |
| Courier's Hut | Courier (Deliveryman) | Delivers items between buildings |
| Warehouse | Warehouse Worker | Central item storage hub |

### Core (4)
| Building | Purpose |
|----------|---------|
| Town Hall | Colony management center, settings, permissions |
| Residence | Citizen housing (capacity scales with level) |
| Mystical Site | Special building, boosts happiness |
| Postbox | Request system player interface |

**All buildings have 5 upgrade levels** (except Postbox at level 1 and Gate House at level 3).

---

## Workers

### All 44 Worker Types

| Worker | Building | Primary Skill | Secondary Skill |
|--------|----------|--------------|-----------------|
| Alchemist | Alchemist Lab | Dexterity | Mana |
| Archer (Training) | Archery | Agility | Adaptability |
| Baker | Bakery | Knowledge | Dexterity |
| Beekeeper | Apiary | Dexterity | Adaptability |
| Blacksmith | Blacksmith Hut | Strength | Focus |
| Builder | Builder's Hut | Adaptability | Athletics |
| Carpenter (=Sawmill) | Sawmill | Knowledge | Dexterity |
| Chef | Kitchen | Creativity | Knowledge |
| Chicken Farmer | Chicken Hut | Adaptability | Agility |
| Composter | Composter Hut | Stamina | Athletics |
| Concrete Mixer | Concrete Hut | Stamina | Dexterity |
| Cook | Dining Hall | Adaptability | Knowledge |
| Courier | Courier Hut | Agility | Adaptability |
| Cowhand | Cowhand Hut | Athletics | Stamina |
| Crusher | Crusher Hut | Stamina | Strength |
| Doctor (Healer) | Hospital | Mana | Knowledge |
| Druid | Guard Tower | — | — |
| Dyer | Dyer Hut | Creativity | Dexterity |
| Enchanter | Enchanter Tower | Mana | Knowledge |
| Farmer | Farmer Hut | Stamina | Athletics |
| Fisher | Fisher Hut | Focus | Agility |
| Fletcher | Fletcher Hut | Dexterity | Creativity |
| Florist | Flowershop | Dexterity | Agility |
| Forester | Forester Hut | Strength | Focus |
| Glassblower | Glassblower Hut | Creativity | Focus |
| Knight | Guard/Barracks | — | — |
| Library Student (Pupil) | Library | Knowledge | Mana |
| Mechanic | Mechanic Hut | Knowledge | Agility |
| Miner | Mine | Strength | Stamina |
| Nether Miner | Nether Mine | Adaptability | Strength |
| Planter | Plantation | Agility | Dexterity |
| Pupil | School | Knowledge | Mana |
| Quarrier | Quarry | Strength | Stamina |
| Rabbit Herder | Rabbit Hutch | Agility | Athletics |
| Researcher | University | Knowledge | Mana |
| Shepherd | Shepherd Hut | Focus | Strength |
| Sifter | Sifter Hut | Focus | Strength |
| Smelter | Smeltery | Athletics | Strength |
| Stone Smelter | Brick Yard | Athletics | Dexterity |
| Stonemason | Stonemason Hut | Creativity | Dexterity |
| Swineherd | Swineherd Hut | Strength | Athletics |
| Teacher | School | Knowledge | Mana |
| Undertaker | Graveyard | Strength | Mana |

---

## Custom Crops (15)

| Crop | Biome | Farmland |
|------|-------|----------|
| Durum | All | Regular |
| Eggplant | All | Regular |
| Garlic | All | Regular |
| Onion | All | Regular |
| Mint | All | Regular |
| Bell Pepper | Temperate | Regular |
| Tomato | Temperate | Regular |
| Corn | Temperate | Regular |
| Cabbage | Cold | Regular |
| Butternut Squash | Cold | Regular |
| Chickpea | Hot Humid | Regular |
| Soy Bean | Hot Humid | Regular |
| Peas | Hot Humid | Regular |
| Rice | Hot Humid | **Flooded Farmland** |
| Nether Pepper | Hot Dry | Regular |

---

## Food Items (95+)

### Tier 1 (Nutrition 5, Saturation 0.6)
Cheddar Cheese, Feta Cheese, Cooked Rice, Tofu, Flatbread, Cheese Ravioli, Chicken Broth, Meat Ravioli, Mint Jelly, Mint Tea, Polenta, Potato Soup, Veggie Ravioli, Yogurt, Squash Soup, Pea Soup, Corn Chowder, Tortillas, Spicy Grilled Chicken

### Tier 2 (Nutrition 7, Saturation 1.0)
Manchet Bread, Lembas Scone, Muffin, Pottage, Pasta Plain, Apple Pie, Plain Cheesecake, Baked Salmon, Eggdrop Soup, Fish & Chips, Pierogi, Veggie Soup, Yogurt with Berries, Cabochis, Veggie Quiche, Rice Ball, Mutton Dinner, Pasta Tomato, Cheese Pizza, Pepper Hummus, Kebab, Congee, Kimchi

### Tier 3 (Nutrition 9, Saturation 1.2)
Hand Pie, Mintchoco Cheesecake, Borscht, Schnitzel, Steak Dinner, Lamb Stew, Fish Dinner, Sushi Roll, Ramen, Eggplant Dolma, Stuffed Pita, Mushroom Pizza, Pita Hummus, Spicy Eggplant, Stew Trencher, Stuffed Pepper, Tacos, Fried Rice

### Ingredients & Breads
Bread Dough, Cookie Dough, Cake Batter, Raw Pumpkin Pie, Butter, Cornmeal, Cream Cheese, Soy Sauce, Raw Noodle, Milky Bread, Sugary Bread, Golden Bread, Chorus Bread

### Bottles
Large Water Bottle, Large Milk Bottle, Large Soy Milk Bottle, Large Empty Bottle

---

## Visitor & Recruitment System

Visitors spawn at your **Tavern** building. Each visitor has a recruitment cost based on tier:

| Tier | Example Items | Count |
|------|--------------|-------|
| 1 | Dried Kelp, Bread, Apple | 1 |
| 2 | Leather, Cooked Fish, Feather | 2 |
| 3 | Cooked Meat, Book | 3 |
| 4 | Iron Ingot, Gold Ingot, Redstone | 4 |
| 5 | Honeycomb, Honey Bottle | 5 |
| 6 | Blaze Powder, Spider Eye | 6 |
| 7 | Diamond, Emerald | 7 |

Higher tavern level = better tier visitors. Unrecruited visitors leave after 3 in-game days.

---

## Quest System

Citizens offer quests through dialogue interactions. Right-click a citizen to talk to them — quest-givers will present dialogue options that advance quest objectives.

### Quest Categories

**Tutorial Quests** (13 quests) — Guide new players through colony setup:
| Quest | What You Learn |
|-------|---------------|
| Welcome | Choose a colony style, use the Build Tool, craft a hut block |
| Builder | Place and upgrade the Builder's Hut |
| Builder 2 | Advanced builder management |
| Housing | Build a Residence for citizens |
| Housing 2 | Expand housing capacity |
| Farm | Set up a Farmer's Hut with crop fields |
| Forester | Set up a Forester's Hut for wood |
| Mine | Establish a Mine for ore extraction |
| Sawmill | Set up wood processing |
| Restaurant | Feed your citizens with a Cook |
| Tiered Food | Craft higher-tier meals for happiness |
| Warehouse | Central storage management |
| Tavern | Recruit new citizens via visitors |
| University | Start researching technologies |

**Military Tutorial** (5 quests):
| Quest | What You Learn |
|-------|---------------|
| Guards | Place a Guard Tower, assign knights |
| Guards 2 | Advanced guard patrol configuration |
| Barracks | Build military housing |
| Torches | Light up your colony for safety |
| Zombies | Defend against the first zombie menace |

**General Quests** (5 quests) — Repeatable colony activities:
| Quest | Objective |
|-------|-----------|
| Alchemy | Brew potions at the Alchemist Lab |
| Cookies | Bake cookies at the Bakery |
| Hungry Courier | Feed a hungry Deliveryman |
| Zombie Menace 1 | Kill zombies threatening the colony |
| Zombie Menace 2 | Eliminate the zombie leader |

**Guide Quests** (4 quests) — Teach item usage:
| Quest | Item Learned |
|-------|-------------|
| Builder Goggles | Using Build Goggles to preview construction |
| Clipboard | Tracking work orders with the Clipboard |
| Rally Banner | Rallying guards with the Banner |
| Resource Scroll | Checking builder material needs |

**Romance Quests** (2 quests) — Citizen relationships:
| Quest | Story |
|-------|-------|
| A Romantic Gesture | Help a citizen impress their crush |
| A Token of Appreciation | Deliver a gift between citizens |

### Quest Mechanics

- **Dialogue-based**: Quests progress through conversation choices with citizens
- **Objectives**: Dialogue responses, block placement, item delivery
- **Rewards**: Items (Quest Log, tools), unlocking follow-up quests
- **Data-driven**: All quests defined in JSON at `data/megamod/colony/quests/`
- **Max occurrences**: Tutorial quests run once; general quests can repeat
- **Quest Log item**: Shift+right-click on a hut block to track active quests

### Creating Custom Quests

Custom quests can be added via datapacks. Place JSON files in `data/<namespace>/colony/quests/`. See `template/questtemplate.json` for the format:

```json
{
  "name": "Quest Name",
  "max-occurrences": 1,
  "parents": [],
  "triggers": [{"type": "megamod:citizen", "state": {}}],
  "objectives": [
    {
      "type": "megamod:dialogue",
      "target": 0,
      "text": "NPC dialogue text",
      "options": [
        {"answer": "Player response", "result": {"type": "megamod:advanceobjective", "go-to": 1}}
      ]
    }
  ],
  "rewards": [
    {"type": "megamod:item", "details": {"item": "minecraft:diamond", "qty": 1}},
    {"type": "megamod:unlockquest", "details": {"id": "megamod:next_quest"}}
  ]
}
```

**Objective Types:**
- `megamod:dialogue` — NPC conversation with player choices
- `megamod:placeblock` — Player must place a specific block
- `megamod:advanceobjective` — Jump to another objective
- `megamod:return` — End conversation, quest stays active

**Reward Types:**
- `megamod:item` — Give player an item
- `megamod:unlockquest` — Unlock a follow-up quest

---

## Colony Statistics

The colony tracks 30+ statistics per day:

Trees Cut, Citizen Deaths, Births, Ores Mined, Blocks Mined, Blocks Placed, Mobs Killed, Items Delivered, Items Crafted, Food Served, Citizens Healed, Crops Harvested, Fish Caught, Buildings Built/Upgraded/Repaired, Arrows Fired/Hit, Lessons Given, Visitors Recruited/Absconded

---

## Enchantments

| Enchantment | Effect | Max Level |
|-------------|--------|-----------|
| Raider Damage | +2.5 bonus damage per level to raider entities | 5 |

---

## Configuration

Colony features can be toggled via the Feature Toggle system:

| Toggle | Default | Description |
|--------|---------|-------------|
| `citizens` | ON | Enable/disable entire citizen system |
| `citizen_territory` | ON | Enable/disable territory claiming |
| `citizen_factions` | ON | Enable/disable faction system |

Additional configuration in `CitizenConfig.java`:
- Max citizens per faction
- Hunger rate and thresholds
- Upkeep costs
- Raid frequency
- Explosion protection level

---

## Admin Commands

Admins (defined in AdminSystem) have full bypass access:

- Break/place blocks in any territory
- Upgrade/repair/rename any building
- Start research for any faction
- Force raids on any colony
- Fulfill requests without items
- Recruit visitors for free
- Claim/unclaim any territory
- Hire/fire/kill any citizen
- Exempt players from upkeep

---

## Custom Citizen Names

Citizen names are data-driven from 22 cultural name files supporting multicultural naming with male/female first names and surnames. Names can be customized via datapacks at `data/megamod/citizennames/`.
