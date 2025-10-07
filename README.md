# FourElements - Advanced Texture Replacement Mod

A Minecraft Fabric mod that allows dynamic texture replacement based on position, neighbors, and block states.

## Installation

1. Download the mod JAR file
2. Place it in your `.minecraft/mods` folder
3. Make sure you have Fabric API installed
4. Launch Minecraft

## Configuration

The configuration file is located at `.minecraft/config/fourelements/texture_replacements.json`.

On first launch, a default configuration with examples will be created automatically.

### Configuration Structure

```json
{
  "rules": [
    {
      "targetBlocks": ["block_name"],
      "positionConditions": [...],
      "neighborConditions": [...],
      "blockStateConditions": [...],
      "replacementTexture": "namespace:block/texture_path"
    }
  ]
}
```

## Rule Types & Examples

### 1. Target Blocks

Specifies which blocks this rule applies to. You can specify multiple blocks.

```json
"targetBlocks": ["stone", "granite", "diorite"]
```

The block name is matched against the block's translation key, so partial matches work (e.g., "stone" matches "stone", "stone_bricks", "sandstone", etc.).

---

### 2. Position Conditions

Replace textures based on block coordinates (X, Y, Z).

#### Available Operators:
- `==` - Equal to
- `>` - Greater than
- `<` - Less than
- `>=` - Greater than or equal to
- `<=` - Less than or equal to
- `%` - Modulo (requires `modulo` field)

#### Example: Checkerboard Pattern (Every Other Y Level)

```json
{
  "targetBlocks": ["stone"],
  "positionConditions": [
    {
      "axis": "y",
      "operator": "%",
      "value": 0,
      "modulo": 2
    }
  ],
  "replacementTexture": "minecraft:block/diamond_block"
}
```

This replaces stone with diamond block texture on even Y coordinates (0, 2, 4, 6, ...).

#### Example: Height-Based Replacement

```json
{
  "targetBlocks": ["oak_planks"],
  "positionConditions": [
    {
      "axis": "y",
      "operator": ">=",
      "value": 64
    },
    {
      "axis": "y",
      "operator": "<=",
      "value": 70
    }
  ],
  "replacementTexture": "minecraft:block/spruce_planks"
}
```

This replaces oak planks with spruce planks texture only between Y levels 64 and 70.

#### Example: Striped Pattern on X Axis

```json
{
  "targetBlocks": ["grass_block"],
  "positionConditions": [
    {
      "axis": "x",
      "operator": "%",
      "value": 0,
      "modulo": 3
    }
  ],
  "replacementTexture": "minecraft:block/moss_block"
}
```

Every 3rd block on the X axis (0, 3, 6, 9, ...) gets replaced.

---

### 3. Neighbor Conditions

Check if specific blocks are adjacent to the target block.

#### Direction Options:
- `UP` - Block above (+Y)
- `DOWN` - Block below (-Y)
- `NORTH` - North direction (-Z)
- `SOUTH` - South direction (+Z)
- `EAST` - East direction (+X)
- `WEST` - West direction (-X)

Or use custom offsets:
- `offsetX` - X offset
- `offsetY` - Y offset
- `offsetZ` - Z offset

#### Example: Dirt Next to Water

```json
{
  "targetBlocks": ["dirt"],
  "neighborConditions": [
    {
      "direction": "UP",
      "targetBlock": "water"
    }
  ],
  "replacementTexture": "minecraft:block/coarse_dirt"
}
```

This replaces dirt with coarse dirt texture when water is directly above it.

#### Example: Custom Offset

```json
{
  "targetBlocks": ["stone"],
  "neighborConditions": [
    {
      "offsetX": 2,
      "offsetY": 0,
      "offsetZ": 1,
      "targetBlock": "lava"
    }
  ],
  "replacementTexture": "minecraft:block/magma_block"
}
```

This checks for lava at position (current + 2X, current + 0Y, current + 1Z).

---

### 4. BlockState Conditions

React to block properties and states at any position! BlockState conditions can check:
- The **current block** (no offset)
- **Neighboring blocks** (using `direction`)
- **Any relative position** (using `offsetX`, `offsetY`, `offsetZ`)

This is incredibly powerful for redstone contraptions and spatial logic!

#### Common Block Properties:
- **Repeater**: `delay` (1-4), `facing` (north/south/east/west), `powered` (true/false), `locked` (true/false)
- **Comparator**: `mode` (compare/subtract), `facing`, `powered`
- **Doors/Trapdoors**: `open` (true/false), `facing`, `half` (upper/lower)
- **Buttons/Levers**: `powered` (true/false), `face` (floor/wall/ceiling)
- **Redstone Wire**: `power` (0-15)
- **Pistons**: `extended` (true/false), `facing`
- **Hoppers**: `enabled` (true/false)
- **Dispensers/Droppers**: `triggered` (true/false), `facing`
- **Note Blocks**: `note` (0-24), `instrument`
- **Observers**: `powered` (true/false), `facing`
- **Torches**: `lit` (true/false)
- **Furnaces**: `lit` (true/false), `facing`

#### Example: Current Block - Repeater with Specific Delay

```json
{
  "targetBlocks": ["repeater"],
  "blockStateConditions": [
    {
      "property": "delay",
      "value": "4"
    }
  ],
  "replacementTexture": "minecraft:block/redstone_block"
}
```

This replaces repeaters with redstone block texture when their delay is set to 4.

#### Example: Current Block - Open Doors

```json
{
  "targetBlocks": ["oak_door"],
  "blockStateConditions": [
    {
      "property": "open",
      "value": "true"
    }
  ],
  "replacementTexture": "minecraft:block/iron_door"
}
```

#### Example: Check Neighbor Block State

```json
{
  "targetBlocks": ["stone"],
  "blockStateConditions": [
    {
      "property": "powered",
      "value": "true",
      "direction": "DOWN"
    }
  ],
  "replacementTexture": "minecraft:block/redstone_block"
}
```

This replaces stone when the block **below** is powered.

#### Example: Check Block State at Custom Position

```json
{
  "targetBlocks": ["diamond_ore"],
  "blockStateConditions": [
    {
      "property": "lit",
      "value": "true",
      "offsetX": 2,
      "offsetY": 0,
      "offsetZ": -1
    }
  ],
  "replacementTexture": "minecraft:block/emerald_ore"
}
```

This checks if a block at position (current + 2X, current + 0Y, current - 1Z) has the `lit` property set to true.

#### Example: Multiple BlockState Checks

```json
{
  "targetBlocks": ["stone"],
  "blockStateConditions": [
    {
      "property": "waterlogged",
      "value": "true"
    },
    {
      "property": "powered",
      "value": "true",
      "direction": "UP"
    }
  ],
  "replacementTexture": "minecraft:block/prismarine"
}
```

This checks if the **current block** is waterlogged AND if the block **above** is powered.

---

### 5. Neighbor BlockState Conditions

**This is where it gets really powerful!** You can check the blockstate of neighboring blocks.

#### Example: Emerald Ore Next to Powered Repeater

```json
{
  "targetBlocks": ["emerald_ore"],
  "neighborConditions": [
    {
      "direction": "NORTH",
      "targetBlock": "repeater",
      "blockStateConditions": [
        {
          "property": "delay",
          "value": "4"
        }
      ]
    }
  ],
  "replacementTexture": "fourelements:block/replacement_texture"
}
```

This replaces emerald ore texture ONLY when there's a repeater to the north with delay set to 4.

#### Example: Advanced Redstone Detection

```json
{
  "targetBlocks": ["stone"],
  "neighborConditions": [
    {
      "direction": "DOWN",
      "targetBlock": "redstone_wire",
      "blockStateConditions": [
        {
          "property": "power",
          "value": "15"
        }
      ]
    }
  ],
  "replacementTexture": "minecraft:block/glowstone"
}
```

This makes stone glow (glowstone texture) when there's fully powered redstone wire below it.

#### Example: Locked Repeater Detection

```json
{
  "targetBlocks": ["diamond_ore"],
  "neighborConditions": [
    {
      "direction": "EAST",
      "targetBlock": "repeater",
      "blockStateConditions": [
        {
          "property": "locked",
          "value": "true"
        },
        {
          "property": "delay",
          "value": "3"
        }
      ]
    }
  ],
  "replacementTexture": "fourelements:block/special_ore"
}
```

Multiple blockStateConditions must ALL be true (AND logic).

---

### 6. Multiple Conditions

You can combine multiple condition types! All conditions must be true (AND logic).

#### Example: Complex Rule

```json
{
  "targetBlocks": ["stone"],
  "positionConditions": [
    {
      "axis": "y",
      "operator": ">=",
      "value": 60
    }
  ],
  "neighborConditions": [
    {
      "direction": "UP",
      "targetBlock": "water"
    }
  ],
  "blockStateConditions": [],
  "replacementTexture": "minecraft:block/prismarine"
}
```

This replaces stone with prismarine texture when:
- It's at Y level 60 or higher AND
- There's water directly above it

---

## Custom Textures

### Using Minecraft Textures

Simply reference any Minecraft texture:

```json
"replacementTexture": "minecraft:block/diamond_block"
```

### Using Custom Textures

1. Create your texture file (16x16 PNG recommended)
2. Place it in a resource pack or in the mod resources at:
   - Resource pack: `.minecraft/resourcepacks/your_pack/assets/fourelements/textures/block/your_texture.png`
   - Or use the config texture folder: `.minecraft/config/fourelements/textures/your_texture.png`

3. Reference it in your config:
```json
"replacementTexture": "fourelements:block/your_texture"
```