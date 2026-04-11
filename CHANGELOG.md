# Changelog

All notable changes to this project are documented in this file.

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
