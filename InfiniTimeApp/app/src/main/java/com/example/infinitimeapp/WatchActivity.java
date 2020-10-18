package com.example.infinitimeapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.example.infinitimeapp.common.Utils;
import com.example.infinitimeapp.services.DeviceInformationService;

import androidx.annotation.Nullable;

public class WatchActivity extends Activity {
    BroadcastReceiver mReceiver;
    TextView manufacturer;
    TextView model;
    TextView serial;
    TextView fw_revision;
    TextView hw_revision;
    TextView sw_revision;

    DeviceInformationService deviceInformationService = DeviceInformationService.getInstance();
    Handler handler = new Handler();
    int DELAY_IN_MILLIS = 50;

    // use this as an inner class like here or as a top-level class
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // do something
            updateDeviceInformation();
        }

        // constructor
        public MyReceiver(){

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        IntentFilter filter = new IntentFilter();
        filter.addAction("action");
        filter.addAction("anotherAction");
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, filter);

        getViewObjects();
        Utils.init();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDeviceInformation();
                handler.postDelayed(this, DELAY_IN_MILLIS);
            }
        }, DELAY_IN_MILLIS);
    }

    private void getViewObjects() {
        manufacturer = (TextView) findViewById(R.id.txt_manufacturer);
        model = (TextView) findViewById(R.id.txt_model);
        serial = (TextView) findViewById(R.id.txt_serial);
        fw_revision = (TextView) findViewById(R.id.txt_fw_revision);
        hw_revision = (TextView) findViewById(R.id.txt_hw_revision);
        sw_revision = (TextView) findViewById(R.id.txt_sw_revision);
    }

    public void updateDeviceInformation() {
        manufacturer.setText("Manufacturer: " + deviceInformationService.mManufacturer);
        model.setText("Model: " + deviceInformationService.mModel);
        serial.setText("Serial Number: " + deviceInformationService.mSerial);
        fw_revision.setText("FW Revision : " + deviceInformationService.mFw_revision);
        hw_revision.setText("HW Revision: " + deviceInformationService.mHw_revision);
        sw_revision.setText("SW Revision: " + deviceInformationService.mSw_revision);
    }
}
