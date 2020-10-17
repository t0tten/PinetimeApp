package com.example.infinitimeapp.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.example.infinitimeapp.MainActivity;
import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.common.Constants;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.TAG;

public class DeviceInformationService implements PinetimeService {
    private static final String MANUFACTURER = "MANUFACTURER";
    private static final String MODEL = "MODEL";
    private static final String SERIAL = "SERIAL";
    private static final String FW_REVISION_ID = "FW_REVISION_ID";
    private static final String HW_REVISION_ID = "HW_REVISION_ID";
    private static final String SW_REVISION_ID = "SW_REVISION_ID";
    private static final Map<String, String> CHAR_MAP = Stream.of(new String[][]{
            {MANUFACTURER, "00002a29-0000-1000-8000-00805f9b34fb"},
            {MODEL, "00002a24-0000-1000-8000-00805f9b34fb"},
            {SERIAL, "00002a25-0000-1000-8000-00805f9b34fb"},
            {FW_REVISION_ID, "00002a26-0000-1000-8000-00805f9b34fb"},
            {HW_REVISION_ID, "00002a27-0000-1000-8000-00805f9b34fb"},
            {SW_REVISION_ID, "00002a28-0000-1000-8000-00805f9b34fb"}
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        Optional<String> key = CHAR_MAP.entrySet().stream()
                .filter(e -> e.getValue().equals(characteristicUUID.toString()))
                .map(Map.Entry::getKey)
                .findFirst();
        return key.get();
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(CHAR_MAP.get(characteristicName));
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case MANUFACTURER:
                Log.i(TAG, new String(message));
                break;
            case MODEL:
                Log.i(TAG, new String(message));
                break;
            case SERIAL:
                Log.i(TAG, new String(message));
                break;
            case FW_REVISION_ID:
                Log.i(TAG, new String(message));
                break;
            case HW_REVISION_ID:
                Log.i(TAG, new String(message));
                break;
            case SW_REVISION_ID:
                Log.i(TAG, new String(message));
                break;
            default:
        }
    }

    public void getManufaturer() {
        sendCall(MANUFACTURER);
    }

    public void getModel() {
        sendCall(MODEL);
    }

    public void getSerial() {
        sendCall(SERIAL);
    }

    public void getFwRevisionId() {
        sendCall(FW_REVISION_ID);
    }

    public void getHwRevisionId() {
        sendCall(HW_REVISION_ID);
    }

    public void getSwRevisionId() {
        sendCall(SW_REVISION_ID);
    }

    private void sendCall(String characteristicUUID) {
        BluetoothService.getInstance().read(getCharacteristicUUID(characteristicUUID), this);
    }
}
