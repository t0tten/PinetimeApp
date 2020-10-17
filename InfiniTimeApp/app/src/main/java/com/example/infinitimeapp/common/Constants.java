package com.example.infinitimeapp.common;

import java.util.UUID;

public interface Constants {
    String TAG = "PINETIME_APP";
    String PINETIME_ROBIN = "E1:88:AF:DF:17:DF";
    String PINETIME_RASMUS = "E1:47:EF:BB:83:AB";

    long SCAN_PERIOD = 10000;

    UUID DEVICE_INFO_SERVICE = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
}
