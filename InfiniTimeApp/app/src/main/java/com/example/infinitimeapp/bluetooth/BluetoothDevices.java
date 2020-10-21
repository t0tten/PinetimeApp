package com.example.infinitimeapp.bluetooth;

import com.example.infinitimeapp.listeners.UpdateUiListener;
import com.example.infinitimeapp.models.BluetoothDevice;

import java.util.ArrayList;

public class BluetoothDevices {
    private static BluetoothDevices sInstance;
    private final ArrayList<BluetoothDevice> mDeviceList;

    private BluetoothDevices() {
        mDeviceList = new ArrayList<>();
    }

    public static BluetoothDevices getInstance() {
        if (sInstance == null) sInstance = new BluetoothDevices();
        return sInstance;
    }

    public void addDevice(BluetoothDevice device) {
        for(BluetoothDevice d: mDeviceList) {
            if(d.getMac().equals(device.getMac())) {
                return;
            }
        }
        mDeviceList.add(device);
        UpdateUiListener.getInstance().getListener().onUpdateUI();
    }

    public BluetoothDevice getDeviceFromIndex(int index) {
        return mDeviceList.get(index);
    }

    public int getSize() {
        return this.mDeviceList.size();
    }

    public void clear() {
        mDeviceList.clear();
    }
}
