package com.zoma1101.music_player.sound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlaybackStartNotifierTest {

    @Test
    void announcesOnlyAfterTrackBecomesActive() {
        PlaybackStartNotifier notifier = new PlaybackStartNotifier();
        notifier.onPlaybackRequested("music/plini_every_piece");

        assertNull(notifier.onHeartbeat(false));
        assertEquals("music/plini_every_piece", notifier.onHeartbeat(true));
        assertNull(notifier.onHeartbeat(true));
    }

    @Test
    void failedRetriesDoNotAnnounceUntilEventuallyActive() {
        PlaybackStartNotifier notifier = new PlaybackStartNotifier();
        notifier.onPlaybackRequested("music/plini_every_piece");

        assertNull(notifier.onHeartbeat(false));
        assertNull(notifier.onHeartbeat(false));

        notifier.onPlaybackRequested("music/plini_every_piece");
        assertNull(notifier.onHeartbeat(false));
        assertEquals("music/plini_every_piece", notifier.onHeartbeat(true));
    }

    @Test
    void resetClearsPendingAnnouncement() {
        PlaybackStartNotifier notifier = new PlaybackStartNotifier();
        notifier.onPlaybackRequested("music/plini_every_piece");
        notifier.reset();

        assertNull(notifier.onHeartbeat(true));
    }
}
