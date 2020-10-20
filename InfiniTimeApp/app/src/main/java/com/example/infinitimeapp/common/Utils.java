package com.example.infinitimeapp.common;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.services.CurrentTimeService;
import com.example.infinitimeapp.services.DeviceInformationService;

public class Utils {

    public static void init(BluetoothService bluetoothService) {
        DeviceInformationService s = DeviceInformationService.getInstance();
        s.getHwRevisionId(bluetoothService);
        s.getFwRevisionId(bluetoothService);
        s.getManufaturer(bluetoothService);
        s.getSerial(bluetoothService);
        s.getSwRevisionId(bluetoothService);
        s.getModel(bluetoothService);

        CurrentTimeService.getInstance().updateTime(bluetoothService);
    }
}
