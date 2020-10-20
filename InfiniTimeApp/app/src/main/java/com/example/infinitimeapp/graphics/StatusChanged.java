package com.example.infinitimeapp.graphics;

import com.example.infinitimeapp.bluetooth.BluetoothService;

public class StatusChanged {
    public interface StatusChangedListener {
        void onConnectionChanged(boolean isConnected, BluetoothService bluetoothService);
        void updateUI();
    }

    static StatusChangedListener mStatusChangesListener;
    public static StatusChanged sInstance = new StatusChanged();

    private StatusChanged() {}

    public void setListener(StatusChangedListener statusChangedListener) {
        StatusChanged.mStatusChangesListener = statusChangedListener;
    }

    public StatusChangedListener getListener() {
        return mStatusChangesListener;
    }

    public static StatusChanged getInstance() {
        return sInstance;
    }
}
