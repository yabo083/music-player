# Music Player

Example soundpack: [Example-SoundPack](https://github.com/zoma1101/Example-SoundPack/tree/main)

This mod plays dynamic background music based on in-game conditions.

## Quick Start

1. Put your soundpack under `.minecraft/soundpacks`
2. Enable the resource pack that matches your `pack_id`
3. Open Minecraft resource pack screen and click `Music Player Pack Settings`

## Soundpack Layout

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

## Playback Mode Control

Use extra fields to control track order:

- `play_mode`: `sequential` / `random` / `specified`
- `play_order`: only used when `play_mode = "specified"`

### Sequential playback

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "sequential"
}
```

### Random playback (avoids immediate repeat)

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "random"
}
```

### Specified playback order

```json
{
  "priority": 10,
  "music": ["music/a.ogg", "music/b.ogg", "music/c.ogg"],
  "play_mode": "specified",
  "play_order": ["music/c.ogg", "0"]
}
```

The example above resolves to:
`music/c.ogg -> music/a.ogg -> music/b.ogg`, then loops.

## Playback Tuning Fields (Per Condition)

You can tune loudness and transition durations per condition:

- `volume`: `0.0` to `1.0` (fallback is current client volume setting)
- `fade_in_ticks`: fade-in duration in game ticks (`20` ticks = `1` second)
- `fade_out_ticks`: fade-out duration in game ticks (`20` ticks = `1` second)

Example:

```json
{
  "priority": 30,
  "music": ["music/night_a.ogg", "music/night_b.ogg"],
  "play_mode": "random",
  "volume": 0.8,
  "fade_in_ticks": 30,
  "fade_out_ticks": 25
}
```

If `fade_in_ticks` / `fade_out_ticks` are missing, the client settings panel values are used.

## Client Playback Controls

From `Music Player Pack Settings`:

- `Music Volume` slider: global MOD music volume
- `Fade In` slider: default fade-in duration
- `Fade Out` slider: default fade-out duration
- `Now Playing HUD` switch: show/hide current track text

These settings are stored in:

- `config/music_player_client_settings.json`

## Now Playing HUD

When enabled, the HUD shows:

- `Now Playing: <track name>`

The HUD is shown only while this MOD's music is actively playing.

## Troubleshooting (No Music)

If dynamic music does not play:

1. Confirm your mod version is `1.2.1` or newer.
2. Check `logs/latest.log`:
   - if you repeatedly see `Track [...] did not become active. Retrying playback.`
   - and never hear audio, you were likely hit by the fade-in startup regression fixed in `1.2.1`.
3. Verify your soundpack OGG files exist at the expected paths.
4. Keep `Music Volume` above `0%` in `Music Player Pack Settings`.

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
