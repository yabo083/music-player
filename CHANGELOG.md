# Changelog

All notable changes to this project are documented in this file.

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

