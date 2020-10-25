package com.example.infinitimeapp.common;

public interface Constants {
    String TAG = "PINETIME_APP";
    String DATABASE_NAME = "PineTimeInfiniTimeCommunicator.db";

    int DATABASE_VERSION = 1;
    int PERMISSIONS_REQUEST_LOCATION = 99;
    int SECOND_IN_MILLIS = 1000;
    int SECONDS_60 = 60;
    int MINUTES_15 = 15;
    int MINUTES_5 = 5;
    int DELAY = MINUTES_5 * SECONDS_60 * SECOND_IN_MILLIS;
}
