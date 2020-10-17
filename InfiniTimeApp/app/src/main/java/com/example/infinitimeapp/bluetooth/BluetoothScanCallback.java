package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import static com.example.infinitimeapp.common.Constants.*;

public class BluetoothScanCallback extends ScanCallback {
    private final BluetoothGattCallback gattCallback;
    private Context context;
    private final BluetoothDevices bluetoothDevices;

    public BluetoothScanCallback(Context context) {
        this.context = context;
        gattCallback = new BluetoothCallback();
        bluetoothDevices = BluetoothDevices.getInstance();
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        // Save device to list
        Log.i(TAG, "Found: " + result.getDevice().getAlias() + ", Address: " + result.getDevice().getAddress());
        if("InfiniTime".equals(result.getDevice().getAlias())) {
            bluetoothDevices.addDevice(result.getDevice());
        }
        //if(Constants.PINETIME_RASMUS.equals(result.getDevice().getAddress())){
            //Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!FOUND: " + result.getDevice().getAddress());

            //Log.i(Constants.TAG, "WATCH FOUND, TRYING TO CONNECT ...");

            // Connect to GATT server
            //result.getDevice().connectGatt(context, true, gattCallback);
        //}
    }
}
