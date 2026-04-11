package com.zoma1101.music_player.client.overlay;

public interface MusicPlaybackObserver {
    void onTrackStarted(String trackDisplayName);

    void onTrackStopped();
}
