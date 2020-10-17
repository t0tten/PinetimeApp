package com.example.infinitimeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.infinitimeapp.adapters.RecycleViewAdapter;
import com.example.infinitimeapp.bluetooth.PinetimeScanner;

public class MainActivity extends AppCompatActivity {
    private boolean isStarted = false;
    static final int REQUEST_ENABLE_BT = 1;
    private BluetoothGatt gatt;
    private PinetimeScanner pinetimeScanner = new PinetimeScanner(MainActivity.this);

    public static RecyclerView recyclerView;
    public static RecycleViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.scanButton);
        button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Looking for Pinetime watches nearby", Toast.LENGTH_LONG).show();
            pinetimeScanner.beginScan();
        });

        recyclerView = (RecyclerView) findViewById(R.id.devicesList);
        mAdapter = new RecycleViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setClickable(true);

        if(!isStarted) {
            isStarted = true;
            final BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }
    
    @Override
    public void onBackPressed() {
        if (gatt == null) {
            return;
        }
        gatt.close();
        gatt = null;
    }
}