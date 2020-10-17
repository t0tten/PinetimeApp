package com.example.infinitimeapp.services;

import android.util.Log;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.TAG;

public class DeviceInformationService extends BaseService {
    private static final String MANUFACTURER = "MANUFACTURER";
    private static final String MODEL = "MODEL";
    private static final String SERIAL = "SERIAL";
    private static final String FW_REVISION_ID = "FW_REVISION_ID";
    private static final String HW_REVISION_ID = "HW_REVISION_ID";
    private static final String SW_REVISION_ID = "SW_REVISION_ID";

    public DeviceInformationService() {
        CHAR_MAP = Stream.of(new String[][]{
                {MANUFACTURER, "00002a29-0000-1000-8000-00805f9b34fb"},
                {MODEL, "00002a24-0000-1000-8000-00805f9b34fb"},
                {SERIAL, "00002a25-0000-1000-8000-00805f9b34fb"},
                {FW_REVISION_ID, "00002a26-0000-1000-8000-00805f9b34fb"},
                {HW_REVISION_ID, "00002a27-0000-1000-8000-00805f9b34fb"},
                {SW_REVISION_ID, "00002a28-0000-1000-8000-00805f9b34fb"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1]));
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
        read(MANUFACTURER);
    }

    public void getModel() {
        read(MODEL);
    }

    public void getSerial() {
        read(SERIAL);
    }

    public void getFwRevisionId() {
        read(FW_REVISION_ID);
    }

    public void getHwRevisionId() {
        read(HW_REVISION_ID);
    }

    public void getSwRevisionId() {
        read(SW_REVISION_ID);
    }
}
