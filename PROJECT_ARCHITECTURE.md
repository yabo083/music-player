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
  - Publishes track start/stop events to playback observers
- `PlaylistNavigator`
  - Computes the next track within a multi-track `MusicDefinition`
  - Supports sequential, random, and specified order modes
- `PlaybackHealthTracker`
  - Guards against transient inactive states
  - Distinguishes between "failed to start" and "natural end"
  - Provides retry/advance decisions to `ClientMusicManager`
- `FadingMusicSoundInstance`
  - Tickable music instance with mutable volume
  - Implements fade-in and fade-out transitions
- `ClientPlaybackSettings`
  - Persists client-side playback defaults (volume, fade durations)
  - Persists `overlay_style` for now-playing text component
  - Serves as fallback values for condition-level tuning fields and HUD style
- `NowPlayingOverlayController`
  - Implements state-driven toast lifecycle (`enter -> hold -> exit`)
  - Consumes music playback observer events instead of polling music manager state
- `NowPlayingOverlayRenderer`
  - Pure render unit that draws text/background from style + computed frame state
- `AnchorLayoutResolver` + `UIAnchor`
  - Resolves 9-grid anchor-based top-left coordinates with X/Y offsets
- `OverlayAnimationResolver`
  - Resolves animation direction by anchor
  - Corner anchors prioritize left/right direction
- `MusicConditionEvaluator`
  - Evaluates rules against player context (biome, time, weather, dimension, entities, GUI, etc.)
- `GameContextHelper` + `CombatStateTracker` + `CombatPulseTracker`
  - Produces the runtime combat flag used by condition matching
  - Combines nearby-mob engagement analysis with client event pulses (player attack / player hurt / enemy hurt by player)
  - Applies a short grace window to avoid rapid enter/exit combat thrash

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

### 3.3 Playback Tuning Fields

- `volume`
  - Per-condition volume multiplier (0.0 to 1.0)
  - Final runtime volume = client global volume × condition volume multiplier
- `fade_in_ticks`
  - Per-condition fade-in duration (ticks)
  - Falls back to client playback settings when omitted/invalid
- `fade_out_ticks`
  - Per-condition fade-out duration (ticks)
  - Falls back to client playback settings when omitted/invalid

## 4. Playback Decision and Data Flow

1. `SoundPackManager` loads each `MusicDefinition` and maps every `music` item to its own `soundEventKey`.
2. `ClientMusicManager` periodically computes the best matched definition (condition + priority), with combat-first precedence:
   - if `isInCombat = true`, it tries `is_combat = true` definitions first
   - if no combat definition matches, it falls back to normal ambient definitions
3. When the matched definition changes:
   - stop current music
   - reset/initialize `PlaylistNavigator`
   - select and play the first track for the new definition
4. During playback health checks:
   - `PlaybackHealthTracker` monitors active/inactive heartbeats
   - if a track never becomes active, the manager retries before skipping
   - if inactivity is confirmed after active playback, it advances with `PlaylistNavigator`
5. On transitions (definition switch, retry, next track):
   - outgoing track is faded out via `FadingMusicSoundInstance`
   - incoming track is started with fade-in and updated `currentMusicSoundEventKey`
6. Event bridge:
   - `ClientMusicManager` emits `onTrackStarted/onTrackStopped` observer events
   - `NowPlayingOverlayController` updates toast state from those events
7. HUD output:
   - `NowPlayingOverlayController` computes animated frame state from `overlay_style`
   - `NowPlayingOverlayRenderer` draws final text/background without accessing music-selection logic

## 5. Compatibility Constraints

- Existing single-track configs remain valid.
- Multi-track support extends only post-match track selection behavior; condition evaluation rules are unchanged.
- `onPlaySound` validates against `currentMusicSoundEventKey` to avoid blocking non-first tracks from the same matched definition.
- If per-condition tuning fields are absent, client playback settings are used as defaults.
