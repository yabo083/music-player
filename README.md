# Music Player

Example soundpack: [Example-SoundPack](https://github.com/zoma1101/Example-SoundPack/tree/main)

This mod plays dynamic background music based on in-game conditions.  
Put your soundpack under `.minecraft/soundpacks`, then enable the resource pack that matches your `pack_id`.

## File Layout

- Condition JSON: `soundpack/pack_id/assets/music_player/conditions/predicate.json`
- OGG files: `soundpack/pack_id/assets/pack_id/music/name.ogg`

## Base Condition JSON

```json
{
  "priority": 100,
  "music": "music/name.ogg"
}
```

`music` supports both forms:

```json
{
  "music": "music/lifeline.ogg"
}
```

```json
{
  "music": ["music/flower_dance.ogg", "music/winter.ogg"]
}
```

## Playback Mode Control (New)

Use extra fields to control how tracks are played:

- `play_mode`: `sequential` / `random` / `specified`
- `play_order`: only used when `play_mode = "specified"`.  
  Each item can be:
  - an index string (for example `"2"`)
  - a track path (for example `"music/winter.ogg"`)

### 1) Sequential playback (loop in `music` array order)

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "sequential"
}
```

### 2) Random playback (avoids immediate repeats)

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "random"
}
```

### 3) Specified playback order

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "specified",
  "play_order": ["music/c.ogg", "0"]
}
```

The example above resolves to: `music/c.ogg -> music/a.ogg -> music/b.ogg`, then loops.

## Available Condition Fields

- `biomes`: target biomes (biome tags supported)
- `is_night`: play only at night
- `is_combat`: play during combat state
- `is_village`: play when villagers are nearby
- `min_y`: minimum Y level (inclusive)
- `max_y`: maximum Y level (inclusive)
- `weather`: weather filter (`clear` / `rain` / `thunder`)
- `dimensions`: target dimensions
- `gui_screen`: play only when a specific GUI is open (`crafting` / `inventory` / `furnace` / `brewing_stand` / `chest` / `creative`)
- `entity_conditions`: entity IDs/tags for nearby entity checks
- `radius`: entity check radius
- `min_count`: minimum entity count
- `max_count`: maximum entity count
