# FourElements Preset System Guide

## Overview

FourElements now uses a modular preset system that makes it easy to create and switch between different texture replacement configurations.

## Folder Structure

```
config/fourelements/
├── config.json              # Main config (selects active preset)
└── presets/
    ├── default/             # Your first preset
    │   ├── rules.json       # Replacement rules for this preset
    │   └── textures/        # Textures for this preset
    │       ├── texture1.png
    │       └── texture2.png
    ├── preset2/             # Another preset
    │   ├── rules.json
    │   └── textures/
    └── my_custom_preset/    # Add as many as you want!
        ├── rules.json
        └── textures/
```

## Creating a New Preset

1. **Create a folder** in `config/fourelements/presets/` with your preset name
   - Example: `config/fourelements/presets/nether_theme/`

2. **Add a `rules.json` file** to define your replacement rules
   ```json
   {
     "rules": [
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
         "replacementTexture": "my_stone.png"
       }
     ]
   }
   ```

3. **Create a `textures/` folder** and add your PNG textures
   - Put your texture files here (e.g., `my_stone.png`)

4. **Reference textures** in your rules:
   - Preset textures: `"my_stone.png"` or `"textures/my_stone.png"`
   - Minecraft textures: `"minecraft:block/diamond_block"`
   - Mod textures: `"modid:block/custom_texture"`

## Switching Presets

### Method 1: Keybinding (Default: P)
- Press `P` in-game to cycle through presets
- Resources will reload automatically
- Configurable in Controls menu

### Method 2: Commands
```
/fourelements cycle         # Cycle to next preset
/fourelements reload        # Reload current preset
/fourelements list          # List all available presets
```

### Method 3: ModMenu GUI
- Open Mod Menu (in-game mods list)
- Find FourElements and click Configure
- Change "Active Preset" field
- Click Save & Apply

## Rules Configuration

Each `rules.json` file contains an array of replacement rules:

### Basic Rule Structure
```json
{
  "targetBlocks": ["block_name"],
  "replacementTexture": "texture.png"
}
```

### Advanced Conditions

#### Position Conditions
Replace blocks based on coordinates:
```json
{
  "targetBlocks": ["stone"],
  "positionConditions": [
    {
      "axis": "y",
      "operator": ">=",
      "value": 64
    }
  ],
  "replacementTexture": "high_altitude_stone.png"
}
```

Operators: `==`, `>`, `<`, `>=`, `<=`, `%` (modulo)

#### Neighbor Conditions
Replace blocks based on adjacent blocks:
```json
{
  "targetBlocks": ["dirt"],
  "neighborConditions": [
    {
      "direction": "UP",
      "targetBlock": "water"
    }
  ],
  "replacementTexture": "wet_dirt.png"
}
```

Directions: `UP`, `DOWN`, `NORTH`, `SOUTH`, `EAST`, `WEST`

#### Block State Conditions
Replace blocks based on their properties:
```json
{
  "targetBlocks": ["oak_door"],
  "blockStateConditions": [
    {
      "property": "open",
      "value": "true"
    }
  ],
  "replacementTexture": "open_door.png"
}
```

### Combining Conditions
You can combine all condition types in a single rule:
```json
{
  "targetBlocks": ["stone", "granite", "andesite"],
  "positionConditions": [
    {
      "axis": "y",
      "operator": "%",
      "value": 0,
      "modulo": 3
    }
  ],
  "neighborConditions": [
    {
      "direction": "DOWN",
      "targetBlock": "lava"
    }
  ],
  "replacementTexture": "hot_stone.png"
}
```

## Tips

1. **Test incrementally**: Start with simple rules and add complexity
2. **Use multiple presets**: Create theme packs (seasonal, biome-specific, etc.)
3. **Share presets**: The entire preset folder can be shared with others
4. **Hot reload**: Use `/fourelements reload` or the keybind to test changes without restarting
5. **Debug mode**: Enable in ModMenu config to see detailed replacement logs

## Example Presets

### Seasonal Theme
Create folders: `spring/`, `summer/`, `autumn/`, `winter/`
Each with seasonal texture variations

### Biome Variants
Create folders: `desert/`, `ocean/`, `mountain/`
Each with biome-appropriate textures

### Connected Textures
Use position modulo to create patterns:
```json
{
  "positionConditions": [
    {"axis": "x", "operator": "%", "value": 0, "modulo": 2},
    {"axis": "z", "operator": "%", "value": 0, "modulo": 2}
  ],
  "replacementTexture": "pattern_topleft.png"
}
```

## Troubleshooting

- **Textures not loading**: Check that PNG files are in the `textures/` folder
- **Rules not applying**: Verify JSON syntax in `rules.json`
- **Missing preset**: Run `/fourelements list` to see available presets
- **Stuck on old preset**: Delete `config.json` to reset to default

## Performance

- Position-only conditions are cached for performance
- Neighbor conditions bypass cache (check on every render)
- Adjust cache size in ModMenu config if needed
- Enable cache stats to monitor performance
