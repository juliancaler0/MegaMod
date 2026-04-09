# General
Addon to https://www.curseforge.com/minecraft/mc-mods/puffish-skills

Contains support for
- https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks

Recommend checking the documentation:
- https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes
- https://puffish.net/skillsmod/docs/creators/configuration/calculations/operations/operation

---

Attributes have been moved
to [Additional Attributes](https://www.curseforge.com/minecraft/mc-mods/additional-attributes)

# Experience Source
## Harvesting Crops
Hooks into the method which gets the items from the loot table

**Operations**:
- `player`: The player harvesting the crops (prototype
  is [Player](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-player))
- `block`: The block being harvested (prototype
  is [Block State](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-block-state))
- `tool`: The tool being used (prototype
  is [Item Stack](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-item-stack))
- `dropped_seeds`: Amount of dropped seeds based on `#forge:seeds` (prototype is `number`)
- `dropped_crops`: Amount of dropped crops based on `#forge:crops` (prototype is `number`)

Example:

```json
{
  "type": "pufferfish_unofficial_additions:harvest_crops",
  "data": {
    "variables": {
      "crops": {
        "operations": [
          {
            "type": "block"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "block": "#minecraft:crops"
            }
          }
        ]
      },
      "dropped_crops": {
        "operations": [
          {
            "type": "dropped_crops"
          }
        ]
      },
      "dropped_seeds": {
        "operations": [
          {
            "type": "dropped_seeds"
          }
        ]
      }
    },
    "experience": [
      {
        "condition": "crops",
        "expression": "dropped_crops + (dropped_seeds * 0.2)"
      }
    ]
  }
}
```

## Fishing Experience
Gets called per fished item

**Operations**:
- `player`: The player who is fishing (prototype
  is [Player](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-player))
- `tool`: The tool being used (prototype
  is [Item Stack](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-item-stack))
- `fished`: The item being fished (prototype
  is [Item Stack](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-item-stack))

Example:

```json
    {
  "type": "pufferfish_unofficial_additions:fishing",
  "data": {
    "variables": {
      "fishes": {
        "operations": [
          {
            "type": "fished"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "item": "#minecraft:fishes"
            }
          }
        ]
      },
      "fished_amount": {
        "operations": [
          {
            "type": "fished"
          },
          {
            "type": "count"
          }
        ]
      }
    },
    "experience": [
      {
        "condition": "fishes",
        "expression": "fished_amount * 4"
      },
      {
        "condition": "!fishes",
        "expression": "fished_amount * 2"
      }
    ]
  }
}
```

# Rewards
## Effect
The reward type is `pufferfish_unofficial_additions:effect`

The data consists of:
- `effect`: The effect in the format of `namespace:path` (e.g. `minecraft:regeneration`)
- `amplifier`: The "level" of the effect
  - For the types `GRANT` and `IMMUNE` this value has to be between `0` and `255`
- `type`: The type of application
  - `GRANT`: The effect will be applied and kept with infinite duration
  - `IMMUNE`: Effect applications equal or below the specified amplifier will not be applied
  - `MODIFY` Effects applications will be modified with the specified amplifier and duration modification
- `duration_modification`: Only applicable to the type `MODIFY` in the format of `<operation><amount>` (e.g. `x1.25` -> multiply by `1.25`)
  - Valid operations are `+`, `-`, `x` and `/` 
  - This element is optional (even for `MODIFY`)

Apply effects (infinite duration):
- Before removing the reward from the definition you'll have to reset the skills (or manually remove the effects yourself)

```json
{
  "type": "pufferfish_unofficial_additions:effect",
  "data": {
    "effect": "minecraft:regeneration",
    "amplifier": 0,
    "type": "GRANT"
  }
}
```

Grant immunity up to a certain amplifier:

```json
{
  "type": "pufferfish_unofficial_additions:effect",
  "data": {
    "effect": "minecraft:wither",
    "amplifier": 1,
    "type": "IMMUNE"
  }
}
```

Modify duration and / or amplifier of incoming effects:
- If the duration reaches `0` due to modifications the effect will not be applied
- If the amplifier goes below `0` due to modifications the effect will not be applied

```json
{
  "type": "pufferfish_unofficial_additions:effect",
  "data": {
    "effect": "minecraft:slowness",
    "amplifier": -2,
    "duration_modification": "/2.5",
    "type": "MODIFY"
  }
}
```

## Walkable Powder Snow
Allows walking on powder snow

```json
{
  "type": "puffish_skills:tag",
  "data": {
    "tag": "walk_on_powder_snow"
  }
}
```

# Iron's Spells 'n Spellbooks
## Experience Sources
Added an experience source for casting spells

**Note**: The experience source will trigger for each spell tick, meaning for continuous spells it can happen multiple
times (see the `expected_ticks` parameter)

**Operations**:
- `player`: The caster (prototype
  is [Player](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-player))
- `main_hand`: Item in main hand (prototype
  is [Item Stack](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-item-stack))
- `spellbook`: The currently equipped spell book (prototype
  is [Item Stack](https://puffish.net/skillsmod/docs/creators/configuration/calculations/prototypes#minecraft-item-stack))
- `school`: The school type of the spell (prototype is custom)
    - The value is defined with `school`
- `spell`: The spell (prototype is custom)
    - The value is defined with `spell`
    - Contains the following sub-operations:
        - `min_level`: The minimum level of this type of spell (prototype is `number`)
        - `max_level`: The maximum level of this type of spell (prototype is `number`)
        - `cast_type`: The way the spell is being cast (`INSTANT`, `LONG`, `CONTINUOUS`) (prototype is `number`)
- `level`: The level of the spell (incl. modifications through items etc.) (prototype is `number`)
- `min_level_rarity`: The minimum level of the spell based on the rarity (prototype is `number`)
- `rarity_name`: Name of the rarity (`COMMON`, `RARE`, `EPIC`, `LEGENDARY`) (prototype is custom)
    - The value is defined with `value`
- `rarity`: The ordinal of the rarity (`0` is `COMMON` and `4` is `LEGENDARY`) (prototype is `number`)
- `mana_cost`: Mana cost for the spell at that level (if the cast consumes mana) (prototype is `number`)
- `mana_cost_per_second`: Only set for continuous spells (and if the cast costs mana) (prototype is `number`)
- `cast_duration`: Duration of continuous spells (in seconds) (prototype is `number`)
- `cast_charge_time`: Charge-up time of spells (if they have one) (in seconds) (prototype is `number`)
- `cooldown`: Cooldown after the spell was cast (in seconds) (prototype is `number`)
- `expected_ticks`: Will be `1` for instant and long type spells - for continuous it's the amount of spell ticks if the
  whole duration is used (prototype is `number`)

Example:

```json
    {
  "type": "pufferfish_unofficial_additions:spell_casting",
  "data": {
    "variables": {
      "level": {
        "operations": [
          {
            "type": "level"
          }
        ]
      },
      "rarity": {
        "operations": [
          {
            "type": "rarity_name"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "value": "LEGENDARY"
            }
          },
          {
            "type": "switch",
            "data": {
              "true": 5,
              "false": 1
            }
          }
        ]
      },
      "mana_cost": {
        "operations": [
          {
            "type": "mana_cost"
          }
        ]
      },
      "mana_cost_per_second": {
        "operations": [
          {
            "type": "mana_cost_per_second"
          }
        ]
      },
      "spellbook": {
        "operations": [
          {
            "type": "spellbook"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "item": "irons_spellbooks:iron_spell_book"
            }
          }
        ]
      },
      "fire_school": {
        "operations": [
          {
            "type": "school"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "school": "irons_spellbooks:fire"
            }
          }
        ]
      },
      "blaze_storm": {
        "operations": [
          {
            "type": "spell"
          },
          {
            "type": "puffish_skills:test",
            "data": {
              "spell": "irons_spellbooks:blaze_storm"
            }
          }
        ]
      }
    },
    "experience": [
      {
        "condition": "spellbook & !blaze_storm & fire_school",
        "expression": "level + (mana_cost / 5) + rarity"
      },
      {
        "condition": "blaze_storm",
        "expression": "(level + (mana_cost_per_second / 10)) / 2"
      }
    ]
  }
}
```