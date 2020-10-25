package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlertNotificationService extends BaseService {
    private static final String NEW_ALERT = "NEW_ALERT";
    private static AlertNotificationService sInstance;

    private AlertNotificationService() {
        super(Stream.of(new String[][]{
                {NEW_ALERT, "00002a46-0000-1000-8000-00805f9b34fb"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1])));
    }

    public static AlertNotificationService getInstance() {
        if (sInstance == null) sInstance = new AlertNotificationService();
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
        message = message.replaceAll("[^a-zA-Z: ]","");
        bluetoothService.write(getCharacteristicUUID(NEW_ALERT), message.getBytes());
    }
}
