package com.example.infinitimeapp.common;

import com.example.infinitimeapp.services.CurrentTimeService;
import com.example.infinitimeapp.services.DeviceInformationService;

public class Utils {

    public static void init() {
        DeviceInformationService s = DeviceInformationService.getInstance();
        s.getHwRevisionId();
        s.getFwRevisionId();
        s.getManufaturer();
        s.getSerial();
        s.getSwRevisionId();
        s.getModel();

        CurrentTimeService.getInstance().updateTime();
    }
}
