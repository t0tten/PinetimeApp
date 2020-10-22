package com.example.infinitimeapp.bluetooth;

import android.content.Context;
import android.util.Log;

import com.example.infinitimeapp.services.MusicService;
import com.example.infinitimeapp.utils.DatabaseConnection;
import com.example.infinitimeapp.WatchActivity;
import com.example.infinitimeapp.listeners.UpdateUiListener;
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
        if(mScanSubscription == null) {
            Log.d(TAG, "Started scanning for bluetooth devices.");
            mScanSubscription = mRxBleClient
                .scanBleDevices(new ScanSettings.Builder().build())
                .subscribe(
                    scanResult -> {
                        RxBleDevice device = scanResult.getBleDevice();
                        if (device.getName() != null && device.getName().contains("InfiniTime")) {
                            Log.d(TAG, "Found " + device.getMacAddress());

                            BluetoothDevice bluetoothDevice = new BluetoothDevice.Builder()
                                .withName(device.getName())
                                .withMac(device.getMacAddress())
                                .build();
                            BluetoothDevices.getInstance().addDevice(bluetoothDevice);
                        }
                    }, Throwable::printStackTrace);
        }
    }

    private void stopScanning() {
        if(mScanSubscription != null) {
            mScanSubscription.dispose();
            mScanSubscription = null;
            Log.d(TAG, "Finished scanning for bluetooth devices.");
        }
    }

    public void connect(String macAddresss) {
        stopScanning();
        RxBleDevice connectedDevice = mRxBleClient.getBleDevice(macAddresss);
        Disposable disposable = connectedDevice.observeConnectionStateChanges()
            .subscribe(
                connectionState -> {
                    if(connectionState != RxBleConnection.RxBleConnectionState.CONNECTED) {
                        this.connect(macAddresss);
                    }
                }, Throwable::printStackTrace);

        mConnectionDisposable = connectedDevice.establishConnection(true)
            .subscribe(
                rxBleConnection -> {
                    mConnection = rxBleConnection;
                    mIsConnected = true;
                    if(WatchActivity.MAC_Address.isEmpty()) {
                        mDatabaseConnection.saveMACToDatabase(macAddresss);
                    }
                    UpdateUiListener.getInstance().getListener().onConnectionChanged(mIsConnected, this);
                }, Throwable::printStackTrace);
    }

    public void listenOnCharacteristic(UUID characteristicUUID) {
        Disposable disposable = mConnection.setupNotification(characteristicUUID)
            .flatMap(notificationObservable -> notificationObservable)
            .subscribe(
                bytes -> {
                    MusicService.getInstance().onDataRecieved(characteristicUUID, bytes);
                }, Throwable::printStackTrace);
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
        Disposable disposable = mConnection.readCharacteristic(characteristicUUID)
            .subscribe(characteristicValue -> {
                service.onDataRecieved(characteristicUUID, characteristicValue);
            }, Throwable::printStackTrace);
    }

    public void write(UUID characteristicUUID, byte[] buffer) {
        if(mConnection == null) {
            return;
        }
        Disposable disposable = mConnection
            .writeCharacteristic(characteristicUUID, buffer)
            .subscribe(characteristicValue -> {}, Throwable::printStackTrace);
    }

    public boolean isConnected() {
        return mIsConnected;
    }
}
