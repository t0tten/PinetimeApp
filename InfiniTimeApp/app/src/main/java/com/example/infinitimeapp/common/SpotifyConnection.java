package com.example.infinitimeapp.common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import static com.example.infinitimeapp.common.Constants.*;

public class SpotifyConnection {
    public class TrackInformation {
        String mArtist;
        String mTrack;
        String mAlbum;

        public TrackInformation(String artist, String track, String album) {
            mArtist = artist;
            mTrack = track;
            mAlbum = album;
        }

        public String getArtist() {
            return mArtist;
        }

        public String getTrack() {
            return mTrack;
        }

        public String getAlbum() {
            return mAlbum;
        }
    };

    private static final String CLIENT_ID = "your_client_id";
    private static final String REDIRECT_URI = "http://com.example.infinitimeapp/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    private boolean isConnected;
    private Context mContext;
    private TrackInformation mTrackInformation;

    public SpotifyConnection(Context context) {
        mContext = context;
        isConnected = false;
        /*ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .showAuthView(true)
                .build();
        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d(TAG, "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        isConnected = true;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });*/
    }

    public void resume() {
        if(isConnected) {
            mSpotifyAppRemote.getPlayerApi().resume();
        }
    }

    public void pause() {
        if(isConnected) {
            mSpotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void nextTrack() {
        if(isConnected) {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    public void previousTrack() {
        if(isConnected) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    public void volumeUp() {
        if(isConnected) {
            mSpotifyAppRemote.getConnectApi().connectIncreaseVolume();
        }
    }

    public void volumeDown() {
        if(isConnected) {
            mSpotifyAppRemote.getConnectApi().connectDecreaseVolume();
        }
    }

    public void getCurrentTrack() {
        if(isConnected) {
            mSpotifyAppRemote.getPlayerApi()
                    .subscribeToPlayerState()
                    .setEventCallback(playerState -> {
                        final Track track = playerState.track;
                        if (track != null) {
                            Log.d("MainActivity", track.name + " by " + track.artist.name);
                            mTrackInformation = new TrackInformation(
                                    track.artist.name,
                                    track.name,
                                    track.album.name);
                            Intent intent = new Intent();
                            intent.setAction("newTackInformation");
                            mContext.sendBroadcast(intent);
                        }
                    });
        }
    }

    public void teardown() {
        if(mSpotifyAppRemote != null) {
            isConnected = false;
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public TrackInformation getTrackInformation() {
        return mTrackInformation;
    }
}
