package com.example.infinitimeapp.bluetooth;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import com.example.infinitimeapp.MainActivity;

import static com.example.infinitimeapp.common.Constants.*;

public class BluetoothScanCallback extends ScanCallback {
    private final BluetoothDevices bluetoothDevices;

    public BluetoothScanCallback() {
        bluetoothDevices = BluetoothDevices.getInstance();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        //Log.i(TAG, "Found: " + result.getDevice().getAlias() + ", Address: " + result.getDevice().getAddress());
        if("InfiniTime".equals(result.getDevice().getAlias())) {
            bluetoothDevices.addDevice(result.getDevice());
            MainActivity.mAdapter.notifyDataSetChanged();
        }
    }
}
