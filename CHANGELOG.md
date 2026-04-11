# Changelog

All notable changes to this project are documented in this file.

## [1.3.3] - 2026-04-12

### Changed
- Reworked combat-state detection to reduce sticky false positives:
  - combat now requires hostile mobs to be engaged with the player (targeting player or recent mutual damage relation),
  - added a short combat-exit grace window to avoid rapid music thrash on edge transitions.

### Added
- Added compatibility aliases for legacy condition field names:
  - `combat` -> `is_combat`
  - `night` -> `is_night`
  - `village` -> `is_village`
- Added regression tests for condition alias parsing and combat grace-window behavior.

## [1.3.2] - 2026-04-11

### Fixed
- Fixed startup/resource-reload ordering issue that could leave `sounds.json` empty on first load.
  - Symptom: repeated `Unable to play unknown soundEvent: music_player:...` and no music.
  - Root cause: dynamic sound data was requested before sound pack discovery completed.
  - Fix: added bootstrap refresh path in `ModSoundResourcePack` to discover packs and populate data whenever sound resources are requested with empty state.

## [1.3.1] - 2026-04-11

### Fixed
- Fixed repeated `Now Playing` fade animation when a track never became active.
  - Root cause: start notification was emitted on playback request, including failed retries.
  - Fix: notify overlay only after the sound instance is confirmed active.
- Fixed retry budget reset on same-track recovery attempts.
  - Root cause: each retry request reset `PlaybackHealthTracker`, causing endless retry loops for broken tracks.
  - Fix: retry state now keeps attempts for the same inactive track key and only resets on real track changes.
- Fixed cases where MOD music and vanilla background music could play together.
  - MOD playback now proactively stops vanilla `MusicManager` when MOD music should take over.

## [1.3.0] - 2026-04-11

### Added
- State-driven `Now Playing` overlay component architecture:
  - `NowPlayingOverlayController` (state/event bridge)
  - `NowPlayingOverlayRenderer` (render-only component)
  - `AnchorLayoutResolver` + `UIAnchor` (9-grid anchor layout)
  - `OverlayAnimationResolver` (anchor-to-animation mapping)
- New in-game `Now Playing Style` screen:
  - 9-grid anchor selection
  - X/Y offset sliders
  - Text RGBA sliders
  - Background toggle + RGBA sliders
  - Enter/Display/Exit tick sliders
  - Slide distance slider
  - Text shadow toggle
  - Live preview with save/cancel behavior
- Added tests for anchor positioning and animation direction mapping.

### Changed
- Decoupled overlay rendering from `ClientMusicManager` via observer pattern (`MusicPlaybackObserver`).
- Replaced static always-on HUD behavior with toast-style timing flow:
  - enter animation
  - hold duration
  - exit animation
- Animation behavior now depends on anchor:
  - center: fade in/out
  - left anchors: slide from/to left
  - right anchors: slide from/to right
  - top center: slide from/to top
  - bottom center: slide from/to bottom
  - corner anchors prioritize left/right behavior.
- Client settings schema upgraded with `overlay_style` and backward-compatible migration from legacy `showNowPlayingHud`.

## [1.2.1] - 2026-04-11

### Fixed
- Fixed a regression where tracks could never start when fade-in was enabled.
  - Root cause: `FadingMusicSoundInstance` started at volume `0` but did not allow silent startup.
  - Result: `SoundManager` never marked the instance active, causing endless retry logs and no audible playback.
  - Fix: explicitly allow silent startup for fade-in/zero-volume startup scenarios.

### Added
- Added regression test for startup policy used by fading music playback.

## [1.2.0] - 2026-04-11

### Added
- Client playback settings panel in `Music Player Pack Settings`:
  - Music volume slider
  - Fade-in duration slider
  - Fade-out duration slider
  - `Now Playing` HUD toggle
- Persistent client settings file: `config/music_player_client_settings.json`
- `Now Playing` HUD text while MOD music is actively playing
- New condition JSON playback tuning fields:
  - `volume`
  - `fade_in_ticks`
  - `fade_out_ticks`

### Changed
- Reworked client playback flow for better stability:
  - Added startup retry path when a track failed to become active
  - Added confirmed-inactive guard before advancing to next track
  - Added fade-aware music instance and transition handling

### Fixed
- Reduced cases where tracks were switched too early.
- Reduced cases where playback stopped unexpectedly after a failed activation.
