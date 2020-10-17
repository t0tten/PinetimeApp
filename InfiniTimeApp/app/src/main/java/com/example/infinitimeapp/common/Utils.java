package com.example.infinitimeapp.common;

import com.example.infinitimeapp.services.CurrentTimeService;
import com.example.infinitimeapp.services.DeviceInformationService;

public class Utils {

    public static void init() {
        DeviceInformationService s = new DeviceInformationService();
        s.getHwRevisionId();
        s.getFwRevisionId();
        s.getManufaturer();
        s.getSerial();

        CurrentTimeService ct = new CurrentTimeService();
        ct.updateTime();
    }
}
