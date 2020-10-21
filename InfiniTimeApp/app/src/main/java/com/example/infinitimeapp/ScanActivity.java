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
import com.example.infinitimeapp.listeners.UpdateUiListener;

import static com.example.infinitimeapp.common.Constants.*;

public class ScanActivity extends AppCompatActivity implements UpdateUiListener.StatusChangedListener {
    public static RecyclerView mViewRecyclerFoundDevices;
    private RecycleViewAdapter mViewRecycleAdapter;
    private BluetoothService mBluetoothService;
    private UpdateUiListener.StatusChangedListener mOldUpdateUiListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        checkLocationAccess();

        mOldUpdateUiListener = UpdateUiListener.getInstance().getListener();
        UpdateUiListener.getInstance().setListener(this);
        mBluetoothService = new BluetoothService(this);

        mViewRecyclerFoundDevices = findViewById(R.id.devicesList);
        Button scanButton = findViewById(R.id.scanButton);

        scanButton.setOnClickListener(v -> {
            Toast.makeText(ScanActivity.this, "Looking for nearby Pinetime devices", Toast.LENGTH_LONG).show();
            mBluetoothService.scan();
            BluetoothDevices.getInstance().clear();
            mViewRecycleAdapter.notifyDataSetChanged();
        });

        mViewRecycleAdapter = new RecycleViewAdapter(this, mBluetoothService);
        mViewRecyclerFoundDevices.setAdapter(mViewRecycleAdapter);
        mViewRecyclerFoundDevices.setLayoutManager(new LinearLayoutManager(this));
        mViewRecyclerFoundDevices.setClickable(true);
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
            UpdateUiListener.getInstance().setListener(mOldUpdateUiListener);
            UpdateUiListener.getInstance().getListener().onConnectionChanged(true, bluetoothService);
            finish();
        }
    }

    @Override
    public void onUpdateUI() {
        mViewRecycleAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSpotifyConnectionChange(boolean isConnected) {
        String message = "";
        if(!isConnected) {
            message = "Could not connect to Remote Spotify";
        } else {
            message = "Connected to Remote Spotify";
        }
        new AlertDialog.Builder(this)
                .setTitle("Remote Spotify")
                .setMessage(message)
                .setNeutralButton("OK", null)
                .show();
    }
}