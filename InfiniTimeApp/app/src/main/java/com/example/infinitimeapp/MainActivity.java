package com.example.infinitimeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainActivity extends AppCompatActivity {
    boolean STARTED = false;
    static final int REQUEST_ENABLE_BT = 1;
    static final long SCAN_PERIOD = 10000;

    /* DeviceInformationService */
    private static final UUID DEVICE_INFO_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    private static final String MANUFACTURER = "MANUFACTURER";
    private static final String MODEL = "MODEL";
    private static final String SERIAL = "SERIAL";
    private static final String FW_REVISION_ID = "FW_REVISION_ID";
    private static final String HW_REVISION_ID = "HW_REVISION_ID";
    private static final String SW_REVISION_ID = "SW_REVISION_ID";

    public static final Map<String, String> DEVICE_INFO_CHAR_MAP = Stream.of(new String[][]{
            {MANUFACTURER, "00002a29-0000-1000-8000-00805f9b34fb"},
            {MODEL, "00002a24-0000-1000-8000-00805f9b34fb"},
            {SERIAL, "00002a25-0000-1000-8000-00805f9b34fb"},
            {FW_REVISION_ID, "00002a26-0000-1000-8000-00805f9b34fb"},
            {HW_REVISION_ID, "00002a27-0000-1000-8000-00805f9b34fb"},
            {SW_REVISION_ID, "00002a28-0000-1000-8000-00805f9b34fb"}
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));


    static final String TAG = "PINETIME_APP";

    private BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private Handler handler = new Handler();
    private boolean mScanning;

    public BluetoothDevice pineTime;
    private BluetoothGatt gatt;

    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    Log.i(TAG, "Found: " + result.getDevice().getAlias() + ", Address: " + result.getDevice().getAddress());
                    //if("E1:47:EF:BB:83:AB".equals(result.getDevice().getAddress())){
                    //if("InfiniTime".equals(result.getDevice().getAlias()))
                    if("E1:88:AF:DF:17:DF".equals(result.getDevice().getAddress())){
                        //Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!FOUND: " + result.getDevice().getAddress());
                        pineTime = result.getDevice();
                        bluetoothLeScanner.stopScan(leScanCallback);

                        Log.i(TAG, "WATCH FOUND, TRYING TO CONNECT ...");

                        // Connect to GATT server
                        gatt = pineTime.connectGatt(MainActivity.this, true, gattCallback);
                    }
                }
            };

    private BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {

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

                        BluetoothGattService devInfoService = gatt.getService(DEVICE_INFO_SERVICE);
                        ArrayList<BluetoothGattCharacteristic> devInfoChars = new ArrayList<>();
                        //devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(MANUFACTURER))));
                        //devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(MODEL))));
                        //devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(SERIAL))));
                        devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(FW_REVISION_ID))));
                        //devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(HW_REVISION_ID))));
                        //devInfoChars.add(devInfoService.getCharacteristic(UUID.fromString(DEVICE_INFO_CHAR_MAP.get(SW_REVISION_ID))));

                        for(BluetoothGattCharacteristic characteristic : devInfoChars) {
                            Log.i(TAG, "Asking watch about Device Information:");
                            if(!gatt.readCharacteristic(characteristic)) {
                                Log.i(TAG, "Could not send read request ...");
                            }
                        }

                    } else {
                        Log.i(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    Optional<String> key = DEVICE_INFO_CHAR_MAP.entrySet().stream()
                            .filter(e -> e.getValue().equals(characteristic.getUuid().toString()))
                            .map(Map.Entry::getKey)
                            .findFirst();

                    Log.i(TAG, "Answer for " + key.get() + " request, STATUS: " + status);
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
            };

    private void scanLeDevice() {
        // Stops scanning after 10 seconds
        if (!mScanning) {
            // Stops scanning after a pre-defined scan period.
            Log.i(TAG, "Starting scan...");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

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

            this.scanLeDevice();
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