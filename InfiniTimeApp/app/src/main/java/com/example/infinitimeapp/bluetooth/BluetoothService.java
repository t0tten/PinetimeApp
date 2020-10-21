package com.example.infinitimeapp.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.example.infinitimeapp.utils.DatabaseConnection;
import com.example.infinitimeapp.WatchActivity;
import com.example.infinitimeapp.graphics.UpdateUiListener;
import com.example.infinitimeapp.models.BluetoothDevice;
import com.example.infinitimeapp.services.PinetimeService;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.UUID;

import io.reactivex.disposables.Disposable;

import static com.example.infinitimeapp.common.Constants.TAG;

public class BluetoothService {
    private final RxBleClient mRxBleClient;
    private final DatabaseConnection mDatabaseConnection;

    private Disposable mScanSubscription;
    private Disposable mConnectionDisposable;
    private RxBleConnection mConnection;
    private boolean mIsConnected;

    public BluetoothService(Context context) {
        mRxBleClient = RxBleClient.create(context);
        mDatabaseConnection = new DatabaseConnection(context);
        mIsConnected = false;
    }

    public void scan() {
        teardown();
        if(mScanSubscription != null) {
            Log.e(TAG, "Error already scanning for bluetooth devices.");
            return;
        }
        Log.d(TAG, "Started scanning for bluetooth devices.");
        mScanSubscription = mRxBleClient.scanBleDevices(
                new ScanSettings.Builder().build()
        )
                .subscribe(
                        scanResult -> {
                            RxBleDevice device = scanResult.getBleDevice();
                            if(device.getName() != null && device.getName().contains("InfiniTime")) {
                                Log.d(TAG, "Found " + device.getMacAddress());

                                BluetoothDevice bluetoothDevice = new BluetoothDevice.Builder()
                                        .withName(device.getName())
                                        .withMac(device.getMacAddress())
                                        .build();
                                BluetoothDevices.getInstance().addDevice(bluetoothDevice);
                            }
                        },
                        throwable -> {
                            Log.d(TAG, throwable.toString());
                        }
                );
    }

    private void stopScanning() {
        if(mScanSubscription != null) {
            mScanSubscription.dispose();
            mScanSubscription = null;
            Log.d(TAG, "Finished scanning for bluetooth devices.");
        }
    }

    @SuppressLint("CheckResult")
    public void connect(String macAddresss) {
        stopScanning();
        RxBleDevice mConnectedDevice = mRxBleClient.getBleDevice(macAddresss);
        mConnectedDevice.observeConnectionStateChanges()
                .subscribe(
                        connectionState -> {
                            Log.d(TAG, "ConnectionState: " + connectionState.toString());
                        },
                        throwable -> {
                            Log.e(TAG, "Error reading connection state: " + throwable);
                        }
                );

        mConnectionDisposable = mConnectedDevice.establishConnection(true)
                .subscribe(
                        rxBleConnection -> {
                            mConnection = rxBleConnection;
                            mIsConnected = true;
                            if(WatchActivity.MAC_Address.isEmpty()) {
                                mDatabaseConnection.saveMACToDatabase(macAddresss);
                            }

                            UpdateUiListener.getInstance().getListener().onConnectionChanged(mIsConnected, this);
                        },
                        e -> {
                            Log.e(TAG, "Error connecting: " + e);
                            e.printStackTrace();
                        }
                );
    }

    private void stopConnection() {
        if(mConnectionDisposable != null) {
            mConnectionDisposable.dispose();
            mConnectionDisposable = null;
            Log.d(TAG, "Teardown connection.");
        }
    }

    public void teardown() {
        mIsConnected = false;
        stopScanning();
        stopConnection();
        UpdateUiListener.getInstance().getListener().onConnectionChanged(mIsConnected, this);
    }

    public void read(UUID characteristicUUID, PinetimeService service) {
        if(mConnection == null) {
            return;
        }
        Disposable disposable = mConnection.readCharacteristic(characteristicUUID).subscribe(
                characteristicValue -> {
                    service.onDataRecieved(characteristicUUID, characteristicValue);
                },
                throwable -> {
                    Log.e(TAG, throwable.toString());
                });
    }

    public void write(UUID characteristicUUID, byte[] buffer) {
        if(mConnection == null) {
            return;
        }
        Disposable disposable = mConnection.writeCharacteristic(characteristicUUID, buffer).subscribe(
                characteristicValue -> {
                    //Log.d(TAG, "Successfully wrote bytes to device.");
                },
                e -> {
                    Log.e(TAG, e.toString());
                    e.printStackTrace();
                }
        );
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
