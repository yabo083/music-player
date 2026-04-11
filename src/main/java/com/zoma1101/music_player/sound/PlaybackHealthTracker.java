package com.zoma1101.music_player.sound;

public class PlaybackHealthTracker {

    public enum Decision {
        NONE,
        RETRY_CURRENT_TRACK,
        ADVANCE_TO_NEXT_TRACK
    }

    private static final int INACTIVE_HEARTBEATS_BEFORE_RETRY = 2;
    private static final int INACTIVE_HEARTBEATS_BEFORE_ADVANCE = 2;
    private static final int MAX_RETRY_ATTEMPTS = 2;

    private boolean trackWasActive = false;
    private int inactiveHeartbeats = 0;
    private int retryAttempts = 0;

    public void reset() {
        trackWasActive = false;
        inactiveHeartbeats = 0;
        retryAttempts = 0;
    }

    public void onTrackRequested() {
        reset();
    }

    public Decision onHeartbeat(boolean isTrackActive) {
        if (isTrackActive) {
            trackWasActive = true;
            inactiveHeartbeats = 0;
            retryAttempts = 0;
            return Decision.NONE;
        }

        inactiveHeartbeats++;

        if (!trackWasActive) {
            if (inactiveHeartbeats < INACTIVE_HEARTBEATS_BEFORE_RETRY) {
                return Decision.NONE;
            }

            inactiveHeartbeats = 0;
            if (retryAttempts < MAX_RETRY_ATTEMPTS) {
                retryAttempts++;
                return Decision.RETRY_CURRENT_TRACK;
            }
            return Decision.ADVANCE_TO_NEXT_TRACK;
        }

        if (inactiveHeartbeats >= INACTIVE_HEARTBEATS_BEFORE_ADVANCE) {
            inactiveHeartbeats = 0;
            return Decision.ADVANCE_TO_NEXT_TRACK;
        }

        return Decision.NONE;
    }
}
