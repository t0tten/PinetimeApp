package com.example.infinitimeapp.utils;

import android.content.Context;
import android.util.Log;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import static com.example.infinitimeapp.common.Constants.*;

public class SpotifyConnection {
    private static final String CLIENT_ID = "";
    private static final String REDIRECT_URI = "http://com.example.infinitimeapp/callback";

    private SpotifyAppRemote mSpotifyAppRemote;
    private boolean mIsConnected;

    public SpotifyConnection(Context context) {
        mIsConnected = false;
        try {
            ConnectionParams connectionParams = new ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(true)
                    .build();

            SpotifyAppRemote.connect(context, connectionParams,
                    new Connector.ConnectionListener() {

                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;
                            mIsConnected = true;
                            Log.d(TAG, "Connected to spotify client");
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e(TAG, throwable.getMessage(), throwable);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "SpotifyConnection: Could not connect");
            e.printStackTrace();
        }
    }

    public void resume() {
        if(mIsConnected) {
            mSpotifyAppRemote.getPlayerApi().resume();
        }
    }

    public void pause() {
        if(mIsConnected) {
            mSpotifyAppRemote.getPlayerApi().pause();
        }
    }

    public void nextTrack() {
        if(mIsConnected) {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    public void previousTrack() {
        if(mIsConnected) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    public void volumeUp() {
        if(mIsConnected) {
            mSpotifyAppRemote.getConnectApi().connectIncreaseVolume();
        }
    }

    public void volumeDown() {
        if(mIsConnected) {
            mSpotifyAppRemote.getConnectApi().connectDecreaseVolume();
        }
    }

    public void teardown() {
        if(mSpotifyAppRemote != null) {
            mIsConnected = false;
            SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
