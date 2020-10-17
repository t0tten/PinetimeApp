package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.example.infinitimeapp.MainActivity;
import com.example.infinitimeapp.services.CurrentTimeService;
import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.PinetimeService;

import java.util.UUID;

import static com.example.infinitimeapp.common.Constants.*;

public class BluetoothCallback extends BluetoothGattCallback {
    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.i(TAG, "onPhyRead");
        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        Log.i(TAG, "onPhyRead");
        super.onPhyRead(gatt, txPhy, rxPhy, status);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            MainActivity.gatt = gatt;
            if(!gatt.discoverServices()) {
                Log.e(TAG, "Could not connect to Watch ...");
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "Disconnected from GATT server.");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "onServicesDiscovered received: GATT_SUCCESS\n");

            DeviceInformationService dis = new DeviceInformationService();
            dis.getFwRevisionId();
        } else {
            Log.i(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        UUID serviceUUID = characteristic.getService().getUuid();

        PinetimeService pinetimeService = null;
        if(DEVICE_INFO_SERVICE.equals(serviceUUID)) {
            pinetimeService = new DeviceInformationService();
        } else if(CURRENT_TIME_SERVICE.equals(serviceUUID)) {
            pinetimeService = new CurrentTimeService();
        }

        String characteristicName = pinetimeService.getCharacteristicName(characteristic.getUuid());
        String message = new String(characteristic.getValue());
        pinetimeService.onDataRecieved(characteristicName, message);

        Log.i(TAG, "Answer for " + characteristicName + " request, STATUS: " + status);
        Log.i(TAG, "\tBYTE VALUE: " + characteristic.getValue().toString());
        Log.i(TAG, "\tSTRING-VALUE: " + message);
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.i(TAG, "Write: " + characteristic.getUuid().toString() + " with status of " + status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, "Change: " + characteristic.getUuid().toString());
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i(TAG, "DescriptorRead: " + descriptor.getUuid().toString());
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.i(TAG, "DescriptorWrite: " + descriptor.getUuid().toString());
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }
}
