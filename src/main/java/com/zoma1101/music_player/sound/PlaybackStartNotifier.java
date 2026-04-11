package com.zoma1101.music_player.sound;

import javax.annotation.Nullable;

public class PlaybackStartNotifier {

    @Nullable
    private String pendingTrackKey = null;
    private boolean announced = false;

    public void onPlaybackRequested(@Nullable String trackKey) {
        pendingTrackKey = trackKey;
        announced = false;
    }

    @Nullable
    public String onHeartbeat(boolean isTrackActive) {
        if (!isTrackActive || announced || pendingTrackKey == null || pendingTrackKey.isBlank()) {
            return null;
        }
        announced = true;
        return pendingTrackKey;
    }

    public void reset() {
        pendingTrackKey = null;
        announced = false;
    }
}
