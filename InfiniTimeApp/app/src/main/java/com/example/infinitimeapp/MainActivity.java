package com.example.infinitimeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.infinitimeapp.bluetooth.BluetoothScanCallback;
import com.example.infinitimeapp.bluetooth.PinetimeScanner;

public class MainActivity extends AppCompatActivity {
    private boolean STARTED = false;
    static final int REQUEST_ENABLE_BT = 1;
    private BluetoothGatt gatt;
    private PinetimeScanner pinetimeScanner = new PinetimeScanner(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!STARTED) {
            STARTED = true;
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            pinetimeScanner.beginScan();
        }
    }


    @Override
    public void onBackPressed() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
    }
}