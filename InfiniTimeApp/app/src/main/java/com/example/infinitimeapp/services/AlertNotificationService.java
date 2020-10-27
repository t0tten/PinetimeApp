package com.example.infinitimeapp.services;

import android.util.Log;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.common.Constants;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlertNotificationService extends BaseService {
    private static final String NEW_ALERT = "NEW_ALERT";

    private static final byte ALERT_UNKNOWN = 0x01;
    private static final byte ALERT_SIMPLE_ALERT = 0x02;
    private static final byte ALERT_EMAIL = 0x03;
    private static final byte ALERT_NEWS = 0x04;
    private static final byte ALERT_INCOMING_CALL = 0x05;
    private static final byte ALERT_MISSED_CALL = 0x06;
    private static final byte ALERT_SMS = 0x07;
    private static final byte ALERT_VOICE_MAIL = 0x08;
    private static final byte ALERT_SCHEDULE = 0x09;
    private static final byte ALERT_HIGH_PRIORITY_ALERT = 0x0a;
    private static final byte ALERT_INSTANT_MESSAGE = 0x0b;

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
        ByteBuffer bb = ByteBuffer.allocate(message.length() + 2);
        bb.put(ALERT_MISSED_CALL);
        bb.put(message.getBytes());

        Log.d(Constants.TAG, message);
        bluetoothService.write(getCharacteristicUUID(NEW_ALERT), bb.array());
    }
}
