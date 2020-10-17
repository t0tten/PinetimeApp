package com.example.infinitimeapp.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.example.infinitimeapp.MainActivity;
import com.example.infinitimeapp.common.Constants;

import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.TAG;

public class CurrentTimeService implements PinetimeService {
    private static final String CURRENT_TIME = "CURRENT_TIME";
    private static final Map<String, String> CHAR_MAP = Stream.of(new String[][]{
            {CURRENT_TIME, "00002a2b-0000-1000-8000-00805f9b34fb"}
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        return CURRENT_TIME;
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(CHAR_MAP.get(characteristicName));
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case CURRENT_TIME:
                break;
            default:
        }
    }

    public void updateTime() {
        BluetoothGattService devInfoService = MainActivity.gatt.getService(Constants.CURRENT_TIME_SERVICE);
        BluetoothGattCharacteristic bgc = devInfoService.getCharacteristic(getCharacteristicUUID(CURRENT_TIME));
        bgc.setValue(getCurrentTime());

        Log.i(TAG, "Asking watch to write Device Information.");
        if(!MainActivity.gatt.writeCharacteristic(bgc)) {
            Log.e(TAG, "Could not send write request ...");
        }
    }

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
}
