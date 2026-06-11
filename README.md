from pathlib import Path

content = r"""# Instant Eat Mod

A Minecraft **NeoForge 1.21.1** mod that allows players to instantly consume the item currently held in their hand using a configurable hotkey.

## Features

### Version 0.0.01a

- Press **G** to instantly consume the item in your hand.
- Food items apply:
    - Hunger restoration
    - POISON restoration
    - CONFUSION restoration
    - Saturation restoration
    - All standard Minecraft food effects
- Consumable items apply their vanilla effects immediately.
- No eating animation or use duration required.
- Designed as a lightweight quality-of-life mod.

## How It Works

Instead of holding the right mouse button and waiting for the eating animation to finish, simply:

1. Hold a consumable item.
2. Press **G**.
3. Receive all normal vanilla effects instantly.

The mod uses Minecraft's existing food and consumable mechanics, ensuring compatibility with vanilla food behavior.

## Supported Effects

Current implementation supports vanilla food functionality:

- Hunger
- Saturation
- Positive status effects
- Negative status effects

Examples:

- Golden Apple effects
- Suspicious Stew effects
- Foods with custom vanilla buffs/debuffs

## Controls

| Key | Action |
|-------|----------|
| G | Instantly consume held item |

## Requirements

- Minecraft 1.21.1
- NeoForge

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Download the latest release.
3. Place the mod `.jar` file into the `mods` folder.
4. Launch the game.

## Roadmap

### Planned Features

- Configurable keybind
- Per-item blacklist
- Per-item whitelist
- Server-side configuration
- Compatibility with modded food items
- Optional eating animation
- Cooldown settings
- Multiplayer synchronization improvements

## Development Status

**Current Version:** `0.0.01a`

This is the first public prototype release intended to validate the core gameplay mechanic.

Feedback, bug reports, and suggestions are welcome.

## License

Choose your preferred license before release.

Recommended options:

- MIT License
- LGPL-3.0
- GPL-3.0

---

Made for Minecraft NeoForge 1.21.1
"""

path = "/mnt/data/README_InstantEat_0.0.01a.md"
Path(path).write_text(content, encoding="utf-8")
print(path)
