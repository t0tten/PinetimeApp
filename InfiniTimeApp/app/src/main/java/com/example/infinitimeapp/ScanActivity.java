package com.example.infinitimeapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.infinitimeapp.graphics.RecycleViewAdapter;
import com.example.infinitimeapp.bluetooth.BluetoothDevices;
import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.graphics.StatusChanged;

public class ScanActivity extends AppCompatActivity implements StatusChanged.StatusChangedListener {
    public final int PERMISSIONS_REQUEST_LOCATION = 99;
    public static RecyclerView recyclerView;

    private RecycleViewAdapter mAdapter;
    private BluetoothService mBluetoothService;
    private StatusChanged.StatusChangedListener oldListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        checkLocationAccess();

        oldListener = StatusChanged.getInstance().getListener();
        StatusChanged.getInstance().setListener(this);
        mBluetoothService = new BluetoothService(this);

        recyclerView = findViewById(R.id.devicesList);
        Button scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v -> {
            Toast.makeText(ScanActivity.this, "Looking for nearby Pinetime devices", Toast.LENGTH_LONG).show();
            mBluetoothService.scan();
            BluetoothDevices.getInstance().clear();
            mAdapter.notifyDataSetChanged();
        });

        mAdapter = new RecycleViewAdapter(this, mBluetoothService);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setClickable(true);
    }

    private void checkLocationAccess() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            new AlertDialog.Builder(this)
                    .setTitle("Need permission")
                    .setMessage("Bluetooth need permission to Location Access.")
                    .setNeutralButton("OK", null)
                    .show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onConnectionChanged(boolean isConnected, BluetoothService bluetoothService) {
        if(isConnected) {
            StatusChanged.getInstance().setListener(oldListener);
            StatusChanged.getInstance().getListener().onConnectionChanged(isConnected, bluetoothService);
            finish();
        }
    }

    @Override
    public void updateUI() {
        mAdapter.notifyDataSetChanged();
    }
}