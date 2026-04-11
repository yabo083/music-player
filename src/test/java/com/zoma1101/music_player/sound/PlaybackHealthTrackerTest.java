package com.zoma1101.music_player.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaybackHealthTrackerTest {

    @Test
    void retriesSameTrackWhenItNeverBecomesActive() {
        PlaybackHealthTracker tracker = new PlaybackHealthTracker();
        tracker.onTrackRequested();

        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.RETRY_CURRENT_TRACK, tracker.onHeartbeat(false));

        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.RETRY_CURRENT_TRACK, tracker.onHeartbeat(false));

        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.ADVANCE_TO_NEXT_TRACK, tracker.onHeartbeat(false));
    }

    @Test
    void advancesTrackOnlyAfterConfirmedInactiveWhenItWasActiveBefore() {
        PlaybackHealthTracker tracker = new PlaybackHealthTracker();
        tracker.onTrackRequested();

        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(true));
        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.ADVANCE_TO_NEXT_TRACK, tracker.onHeartbeat(false));
    }

    @Test
    void activeHeartbeatResetsTransientInactiveState() {
        PlaybackHealthTracker tracker = new PlaybackHealthTracker();
        tracker.onTrackRequested();

        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(true));
        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(true));
        assertEquals(PlaybackHealthTracker.Decision.NONE, tracker.onHeartbeat(false));
        assertEquals(PlaybackHealthTracker.Decision.ADVANCE_TO_NEXT_TRACK, tracker.onHeartbeat(false));
    }
}
