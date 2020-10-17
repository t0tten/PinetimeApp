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
import android.widget.Button;
import android.widget.Toast;

import com.example.infinitimeapp.adapters.RecycleViewAdapter;
import com.example.infinitimeapp.bluetooth.BluetoothDevices;
import com.example.infinitimeapp.bluetooth.BluetoothService;

public class MainActivity extends AppCompatActivity {
    private boolean isStarted = false;
    static final int REQUEST_ENABLE_BT = 1;

    public static RecyclerView recyclerView;
    public static RecycleViewAdapter mAdapter;
    BluetoothService mBluetoothService = BluetoothService.getInstance();
    public static BluetoothGatt gatt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBluetoothService.init(this);
        setContentView(R.layout.activity_main);

        final Button button = findViewById(R.id.scanButton);
        button.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Looking for Pinetime watches nearby", Toast.LENGTH_LONG).show();
            mBluetoothService.scan();
            BluetoothDevices.getInstance().clear();
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
    public void onDestroy() {
        mBluetoothService.teardown();
        super.onDestroy();
    }
}