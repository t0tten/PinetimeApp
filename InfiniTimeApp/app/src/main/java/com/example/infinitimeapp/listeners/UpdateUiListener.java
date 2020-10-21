package com.example.infinitimeapp.listeners;

import com.example.infinitimeapp.bluetooth.BluetoothService;

public class UpdateUiListener {
    public interface StatusChangedListener {
        void onConnectionChanged(boolean isConnected, BluetoothService bluetoothService);
        void onUpdateUI();
        void onSpotifyConnectionChange(boolean isConnected);
    }

    public static UpdateUiListener sInstance;
    static StatusChangedListener mStatusChangesListener;

    private UpdateUiListener() {}

    public void setListener(StatusChangedListener statusChangedListener) {
        UpdateUiListener.mStatusChangesListener = statusChangedListener;
    }

    public StatusChangedListener getListener() {
        return mStatusChangesListener;
    }

    public static UpdateUiListener getInstance() {
        if (sInstance == null) sInstance = new UpdateUiListener();
        return sInstance;
    }
}
