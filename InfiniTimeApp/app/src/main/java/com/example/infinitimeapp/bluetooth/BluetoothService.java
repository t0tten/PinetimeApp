package com.example.infinitimeapp.bluetooth;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.infinitimeapp.database.Database;
import com.example.infinitimeapp.ScanActivity;
import com.example.infinitimeapp.WatchActivity;
import com.example.infinitimeapp.common.Utils;
import com.example.infinitimeapp.services.PinetimeService;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.UUID;

import io.reactivex.disposables.Disposable;

import static com.example.infinitimeapp.common.Constants.TAG;

public class BluetoothService {

    private static BluetoothService instance = null;

    RxBleClient mRxBleClient;
    Context mContext;
    Disposable mScanSubscription = null;
    Disposable mConnectionDisposable = null;
    RxBleDevice mConnectedDevice = null;
    RxBleConnection mConnection = null;
    boolean isConnected;

    private BluetoothService() {
        isConnected = false;
    }

    public static BluetoothService getInstance()
    {
        if (instance == null)
            instance = new BluetoothService();
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        mRxBleClient = RxBleClient.create(context);
    }

    public void scan() {
        teardown();
        if(mScanSubscription != null) {
            Log.e(TAG, "Error already scanning for bluetooth devices.");
            return;
        }

        Log.i(TAG, "Started scanning for bluetooth devices.");
        mScanSubscription = mRxBleClient.scanBleDevices(
                new ScanSettings.Builder().build()
        )
                .subscribe(
                        scanResult -> {
                            RxBleDevice device = scanResult.getBleDevice();
                            if(device.getName() != null && device.getName().contains("InfiniTime")) {
                                Log.i(TAG, "Found " + device.getMacAddress());

                                BluetoothDevices.BTDeviceModel d = new BluetoothDevices.BTDeviceModel(device.getMacAddress(), device.getName());
                                BluetoothDevices.getInstance().addDevice(d);
                                ScanActivity.mAdapter.notifyDataSetChanged();
                            }
                        },
                        throwable -> {
                            Log.i(TAG, throwable.toString());
                        }
                );
    }

    private void stopScanning() {
        if(mScanSubscription != null) {
            mScanSubscription.dispose();
            mScanSubscription = null;
            Log.i(TAG, "Finished scanning for bluetooth devices.");
        }
    }

    public void connect(String macAddresss) {
        stopScanning();

        mConnectedDevice = mRxBleClient.getBleDevice(macAddresss);

         mConnectedDevice.observeConnectionStateChanges()
                .subscribe(
                        connectionState -> {
                            Log.i(TAG, connectionState.toString());
                        },
                        throwable -> {
                            Log.e(TAG, "Error reading connection state: " + throwable);
                        }
                );

        mConnectionDisposable = mConnectedDevice.establishConnection(true)
                .subscribe(
                        rxBleConnection -> {
                            Log.i(TAG, "Connected to " + macAddresss);
                            mConnection = rxBleConnection;
                            isConnected = true;
                            if(WatchActivity.MAC_Address.isEmpty()) {
                                Database database = new Database(mContext);
                                database.saveMACToDatabase(macAddresss);
                            }
                            Utils.init();
                        },
                        throwable -> {
                            Log.e(TAG, "Error connecting: " + throwable);
                        }
                );
    }

    private void stopConnection() {
        if(mConnectionDisposable != null) {
            mConnectionDisposable.dispose();
            mConnectionDisposable = null;
            Log.i(TAG, "Teardown connection.");
        }
    }

    public void teardown() {
        isConnected = false;
        stopScanning();
        stopConnection();
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
                    Log.i(TAG, "Successfully wrote bytes to device.");
                },
                throwable -> {
                    Log.e(TAG, throwable.toString());
                }
        );
    }

    public boolean isConnected() {
        return isConnected;
    }
}
