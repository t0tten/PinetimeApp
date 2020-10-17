package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;

public class BluetoothDevices {
    private static BluetoothDevices instance = new BluetoothDevices();
    private final ArrayList<BluetoothDevice> deviceList;

    private BluetoothDevices() {
        deviceList = new ArrayList<>();
    }

    public static BluetoothDevices getInstance() {
        return instance;
    }

    public ArrayList<BluetoothDevice> getDevices(){
        return this.deviceList;
    }

    public void addDevice(BluetoothDevice device) {
        deviceList.add(device);
    }

    public BluetoothDevice getDeviceFromAddress(String address) {
        BluetoothDevice btDevice = null;

        for (BluetoothDevice device : deviceList) {
            if(device.getAddress().equals(address)) {
                btDevice = device;
                break;
            }
        }
        return btDevice;
    }
}
