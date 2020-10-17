package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import static com.example.infinitimeapp.common.Constants.*;

public class PinetimeScanner {
    private final BluetoothLeScanner SCANNER;
    private Handler handler;
    private final BluetoothScanCallback scanCallback;

    private boolean isScanning = false;

    public PinetimeScanner(Context context) {
        scanCallback = new BluetoothScanCallback();
        handler = new Handler();
        SCANNER = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    }

    public void beginScan() {
        if (!isScanning) {
            Log.i(TAG, "Starting scan...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanner();
                }
            }, SCAN_PERIOD);
            startScanner();
        } else {
            stopScanner();
        }
    }

    private void startScanner() {
        isScanning = true;
        SCANNER.startScan(scanCallback);
    }
    public void stopScanner() {
        isScanning = false;
        SCANNER.stopScan(scanCallback);
    }
}
