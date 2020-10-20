package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class AlertNotificationService extends BaseService {
    public static final String NEW_ALERT = "NEW_ALERT";

    private static AlertNotificationService sInstance = null;

    private AlertNotificationService() {
        CHAR_MAP.put(NEW_ALERT, "00002a46-0000-1000-8000-00805f9b34fb");
    }

    public static AlertNotificationService getInstance() {
        if (sInstance == null)
            sInstance = new AlertNotificationService();

        return sInstance;
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case NEW_ALERT:
                break;
            default:
        }
    }

    public void sendMessage(BluetoothService bluetoothService, String message) {
        bluetoothService.write(getCharacteristicUUID(NEW_ALERT), message.getBytes(StandardCharsets.US_ASCII));
    }
}
