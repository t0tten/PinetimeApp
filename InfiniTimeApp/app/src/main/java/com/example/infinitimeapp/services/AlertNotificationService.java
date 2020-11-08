package com.example.infinitimeapp.services;

import android.annotation.SuppressLint;
import android.telecom.TelecomManager;
import android.util.Log;

import com.example.infinitimeapp.WatchActivity;
import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.*;

public class AlertNotificationService extends BaseService {
    private static final String NEW_ALERT = "NEW_ALERT";
    private static final String EVENT = "EVENT";

    public static final byte PADDING = 0x00;
    public static final byte ALERT_UNKNOWN = 0x01;
    public static final byte ALERT_SIMPLE_ALERT = 0x02;
    public static final byte ALERT_EMAIL = 0x03;
    public static final byte ALERT_NEWS = 0x04;
    public static final byte ALERT_INCOMING_CALL = 0x05;
    public static final byte ALERT_MISSED_CALL = 0x06;
    public static final byte ALERT_SMS = 0x07;
    public static final byte ALERT_VOICE_MAIL = 0x08;
    public static final byte ALERT_SCHEDULE = 0x09;
    public static final byte ALERT_HIGH_PRIORITY_ALERT = 0x0a;
    public static final byte ALERT_INSTANT_MESSAGE = 0x0b;

    private static AlertNotificationService sInstance;

    private AlertNotificationService() {
        super(Stream.of(new String[][]{
                {NEW_ALERT, "00002a46-0000-1000-8000-00805f9b34fb"},
                {EVENT, "00002a47-0000-1000-8000-00805f9b34fb"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1])));
    }

    public static AlertNotificationService getInstance() {
        if (sInstance == null) sInstance = new AlertNotificationService();
        return sInstance;
    }

    public UUID getEventUUID() {
        return getCharacteristicUUID(EVENT);
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch (getCharacteristicName(characteristicName)) {
            case NEW_ALERT:
                break;
            case EVENT:
                eventHandler(message);
                break;
            default:
        }
    }

    private void eventHandler(byte[] message) {
        final byte EVENT_HANG_UP_CALL = 0x00;
        final byte EVENT_ANSWER_CALL = 0x01;

        switch (message[0]) {
            case EVENT_ANSWER_CALL:
                Log.d(TAG, "ANSWER CALL!");
                answerPhoneCall();
                break;
            case EVENT_HANG_UP_CALL:
                Log.d(TAG, "HANG UP CALL!");
                hangUpPhoneCall();
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void answerPhoneCall() {
        TelecomManager telecomManager = WatchActivity.sTelecomManager;
        if(telecomManager != null) {
            telecomManager.acceptRingingCall();
        }
    }

    @SuppressLint("MissingPermission")
    private void hangUpPhoneCall() {
        TelecomManager telecomManager = WatchActivity.sTelecomManager;
        if(telecomManager != null) {
            Log.d(TAG, "HANG UP CALL! INNE");
            telecomManager.endCall();
        }
    }

    public void sendMessage(BluetoothService bluetoothService, String message) {
        sendMessage(bluetoothService, message, ALERT_INCOMING_CALL);
    }

    public void subscribeOnEvents(BluetoothService bluetoothService) {
        bluetoothService.listenOnCharacteristic(getCharacteristicUUID(EVENT));
    }

    public void sendMessage(BluetoothService bluetoothService, String message, byte category) {
        ByteBuffer bb = ByteBuffer.allocate(message.length() + 4);
        bb.put(category);
        bb.put(PADDING);
        bb.put(PADDING);
        bb.put(message.getBytes());

        Log.d(TAG, message);
        bluetoothService.write(getCharacteristicUUID(NEW_ALERT), bb.array());
    }
}
