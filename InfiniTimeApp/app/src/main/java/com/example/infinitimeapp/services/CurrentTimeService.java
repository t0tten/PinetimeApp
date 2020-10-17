package com.example.infinitimeapp.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.example.infinitimeapp.MainActivity;
import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.common.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.infinitimeapp.common.Constants.TAG;

public class CurrentTimeService extends BaseService {
    private static final String CURRENT_TIME = "CURRENT_TIME";

    public CurrentTimeService() {
        CHAR_MAP.put(CURRENT_TIME, "00002a2b-0000-1000-8000-00805f9b34fb");
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case CURRENT_TIME:
                break;
            default:
        }
    }

    byte[] getCTSAsBytes() {
        Calendar time = Calendar.getInstance();

        int dayOfWeek = time.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.SUNDAY) {
            dayOfWeek = 7;
        } else {
            dayOfWeek = dayOfWeek - 1;
        }

        return ByteBuffer.allocate(10)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort((short) time.get(Calendar.YEAR))
                .put((byte) (time.get(Calendar.MONTH) + 1))
                .put((byte) time.get(Calendar.DAY_OF_MONTH))
                .put((byte) time.get(Calendar.HOUR_OF_DAY))
                .put((byte) time.get(Calendar.MINUTE))
                .put((byte) time.get(Calendar.SECOND))
                .put((byte) dayOfWeek)
                .put((byte) (int)((time).get(Calendar.MILLISECOND) * 0.255F))
                .put((byte) 0)
                .array();
    }

    public void updateTime() {
        BluetoothService.getInstance().write(getCharacteristicUUID(CURRENT_TIME), getCTSAsBytes());
    }
}
