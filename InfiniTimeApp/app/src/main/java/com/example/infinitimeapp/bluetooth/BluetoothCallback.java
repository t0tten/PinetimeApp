package com.example.infinitimeapp.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.PinetimeService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import static com.example.infinitimeapp.common.Constants.*;

public class BluetoothCallback extends BluetoothGattCallback {

    private byte ConvertDecimal2BCD(byte decimal) {
        byte result = 0;
        result += (decimal % 10);
        result += (decimal / 10 << 0x4);
        return result;
    }

    private byte[] getCurrentTime () {
        byte[] values = new byte[20];
        Calendar dateTime = Calendar.getInstance();
        //values[0] = COMMAND_ID_SETTING_TIME;
        values[1] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.YEAR));
        values[1] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.MONTH));
        values[2] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.DAY_OF_MONTH));
        values[3] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.HOUR_OF_DAY));
        values[4] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.MINUTE));
        values[5] = ConvertDecimal2BCD((byte) dateTime.get(Calendar.SECOND));
        return values;
    }

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
            if(!gatt.discoverServices()) {
                Log.i(TAG, "Could not connect to Watch ...");
            }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i(TAG, "Disconnected from GATT server.");
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "onServicesDiscovered received: GATT_SUCCESS\n");
            ArrayList<BluetoothGattService> services = new ArrayList<>(gatt.getServices());
            for(BluetoothGattService service : services) {
                //Log.i(TAG, "Service: " + service.getUuid().toString());
                ArrayList<BluetoothGattCharacteristic> characteristics = new ArrayList<>(service.getCharacteristics());
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String uuid_str = characteristic.getUuid().toString();
                    //Log.i(TAG, "\tUUID: " + uuid_str);
                    if (uuid_str.contains("00002a2b")) {
                        //Log.i(TAG, "\tCLOCK: ");

                        // Attempt to write time
                                    /*if(characteristic.setValue(getCurrentTime())) {
                                        Log.i(TAG, "\t\tSaved value!");
                                        if (gatt.writeCharacteristic(characteristic)) {
                                            Log.i(TAG, "\t\tNew value sent!!");
                                        } else {
                                            Log.i(TAG, "\t\tFUCK OFF AND DIE!");
                                        }
                                    }*/
                    }
                    //Log.i(TAG, "\tPERMISSION: " + characteristic.getPermissions());
                    //Log.i(TAG, "\tPROPERTIES: " + characteristic.getProperties());
                }
            }

            PinetimeService pinetimeService = new DeviceInformationService();
            BluetoothGattService devInfoService = gatt.getService(DeviceInformationService.SERVICE_UUID);
            BluetoothGattCharacteristic bgc = devInfoService.getCharacteristic(pinetimeService.getCharacteristicUUID(DeviceInformationService.FW_REVISION_ID));

            Log.i(TAG, "Asking watch about Device Information:");
            if(!gatt.readCharacteristic(bgc)) {
                Log.i(TAG, "Could not send read request ...");
            }

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

        }

        String characteristicName = pinetimeService.getCharacteristicName(characteristic.getUuid());
        Log.i(TAG, "Answer for " + characteristicName + " request, STATUS: " + status);
        Log.i(TAG, "\tBYTE VALUE: " + characteristic.getValue().toString());
        Log.i(TAG, "\tSTRING-VALUE: " + new String(characteristic.getValue()));
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
