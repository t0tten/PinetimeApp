package com.example.infinitimeapp.listeners;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.infinitimeapp.common.Constants;
import com.example.infinitimeapp.models.TrackInformation;

public class SpotifyBroadcastReceiver extends android.content.BroadcastReceiver {
    public interface ReceiverListener {
        void onPlayingStateChanged(boolean isPlaying);
        void onTrackChanged(TrackInformation trackInformation);
    }

    private static ReceiverListener mReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        final String SPOTIFY_PACKAGE = "com.spotify.music";
        final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";

        final String action = intent.getAction();
        if (action.equals(METADATA_CHANGED)) {
            final String ARTIST = "artist";
            final String TRACK = "track";
            final String ALBUM = "album";

            TrackInformation trackInformation = new TrackInformation.Builder()
                    .withArtist(intent.getStringExtra(ARTIST))
                    .withTrack(intent.getStringExtra(TRACK))
                    .withAlbum(intent.getStringExtra(ALBUM))
                    .build();
            mReceiverListener.onTrackChanged(trackInformation);
        } else if (action.equals(PLAYBACK_STATE_CHANGED)) {
            final String PLAYING = "playing";
            boolean isPlaying = intent.getBooleanExtra(PLAYING, false);
            mReceiverListener.onPlayingStateChanged(isPlaying);
        }
    }

    public void setListener(ReceiverListener mReceiverListener) {
        SpotifyBroadcastReceiver.mReceiverListener = mReceiverListener ;
    }
}
