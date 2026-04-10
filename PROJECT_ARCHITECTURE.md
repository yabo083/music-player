# PROJECT ARCHITECTURE

## 1. System Goal

`music-player` loads condition JSON files from external soundpacks and selects background music at runtime on the client side.  
The architecture separates condition matching from audio resource provisioning, so users can add or change music behavior without modifying mod code.

## 2. Core Module Topology

- `SoundPackManager`
  - Discovers and loads soundpacks (folder + zip)
  - Parses `conditions/*.json` into `MusicDefinition`
  - Generates dynamic `sounds.json` content and keeps the OGG resource map
- `MusicDefinition`
  - Data model for one condition rule
  - Holds match conditions, music list, and playback mode
- `ClientMusicManager`
  - Tick-driven client playback controller
  - Computes the best active definition, blocks conflicting music sources, and handles play/stop transitions
- `PlaylistNavigator`
  - Computes the next track within a multi-track `MusicDefinition`
  - Supports sequential, random, and specified order modes
- `MusicConditionEvaluator`
  - Evaluates rules against player context (biome, time, weather, dimension, entities, GUI, etc.)

## 3. Condition JSON Contract (Architecture Level)

### 3.1 `music` Field

- Backward-compatible: supports both string and array input
  - String: `"music": "music/lifeline.ogg"`
  - Array: `"music": ["music/a.ogg", "music/b.ogg"]`

### 3.2 Playback Strategy Fields

- `play_mode`
  - `sequential`: loop in `music` list order
  - `random`: random track selection (no immediate repeat)
  - `specified`: use explicit order from `play_order`
- `play_order`
  - Used when `play_mode = "specified"`
  - Accepts mixed item types:
    - index string (for example `"2"`)
    - track path (for example `"music/winter.ogg"`)
  - Any tracks not covered in `play_order` are appended in original `music` order

## 4. Playback Decision and Data Flow

1. `SoundPackManager` loads each `MusicDefinition` and maps every `music` item to its own `soundEventKey`.
2. `ClientMusicManager` periodically computes the best matched definition (condition + priority).
3. When the matched definition changes:
   - stop current music
   - reset/initialize `PlaylistNavigator`
   - select and play the first track for the new definition
4. When the current track ends naturally:
   - `PlaylistNavigator` computes the next track from `play_mode`
   - the manager plays that next track and updates `currentMusicSoundEventKey`

## 5. Compatibility Constraints

- Existing single-track configs remain valid.
- Multi-track support extends only post-match track selection behavior; condition evaluation rules are unchanged.
- `onPlaySound` validates against `currentMusicSoundEventKey` to avoid blocking non-first tracks from the same matched definition.
