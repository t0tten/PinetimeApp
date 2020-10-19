package com.example.infinitimeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.Toast;

import com.example.infinitimeapp.adapters.RecycleViewAdapter;
import com.example.infinitimeapp.bluetooth.BluetoothDevices;
import com.example.infinitimeapp.bluetooth.BluetoothService;

import static com.example.infinitimeapp.common.Constants.DELAY_IN_MILLIS;

public class ScanActivity extends AppCompatActivity {
    public static RecyclerView recyclerView;
    public static RecycleViewAdapter mAdapter;
    BluetoothService mBluetoothService;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        mBluetoothService = BluetoothService.getInstance();
        mBluetoothService.init(this);
        recyclerView = findViewById(R.id.devicesList);
        Button scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v -> {
            Toast.makeText(ScanActivity.this, "Looking for nearby Pinetime devices", Toast.LENGTH_LONG).show();
            mBluetoothService.scan();
            BluetoothDevices.getInstance().clear();
        });

        mAdapter = new RecycleViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setClickable(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(BluetoothService.getInstance().isConnected()) {
                    finish();
                }
                handler.postDelayed(this, DELAY_IN_MILLIS);
            }
        }, DELAY_IN_MILLIS);
    }
}