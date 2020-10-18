package com.example.infinitimeapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.database.Database;
import com.example.infinitimeapp.services.AlertNotificationService;
import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.MusicService;

import androidx.annotation.Nullable;

import static com.example.infinitimeapp.common.Constants.DELAY_IN_MILLIS;
import static com.example.infinitimeapp.common.Constants.TAG;

public class WatchActivity extends Activity {
    TextView manufacturer;
    TextView model;
    TextView serial;
    TextView fw_revision;
    TextView hw_revision;
    TextView sw_revision;

    EditText alertMessage;
    EditText musicTrack;
    EditText musicArtist;
    EditText musicAlbum;

    Button disconnectButton;
    Button sendAlert;
    Button sendMusicInfo;

    DeviceInformationService deviceInformationService = DeviceInformationService.getInstance();
    AlertNotificationService alertNotificationService = AlertNotificationService.getInstance();

    Handler handler = new Handler();
    Database database = new Database(this);

    static boolean connectionState = true;
    static final int REQUEST_ENABLE_BT = 1;
    public static String MAC_Address = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        getViewObjects();
        applyButtonClickListers();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDeviceInformation();
                handler.postDelayed(this, DELAY_IN_MILLIS);
            }
        }, DELAY_IN_MILLIS);
    }



    private void applyButtonClickListers() {
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BluetoothService.getInstance().teardown();
                showToast("Disconnecting from watch");
                connectionState = false;
                enableDisableUI(false);
                database.removeMacFromDatabase();
            }
        });

        sendAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = alertMessage.getText().toString();
                if(message.isEmpty()) {
                    Log.e(TAG, "Message is empty");
                    showEmptyErrorAlert();
                    return;
                }
                showToast("Sending alert: " + message);
                alertMessage.setText("");
                alertNotificationService.sendMessage(message);
            }
        });

        sendMusicInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String track = musicTrack.getText().toString();
                String artist = musicArtist.getText().toString();
                String album = musicAlbum.getText().toString();

                if(track.isEmpty() && artist.isEmpty() && album.isEmpty()) {
                    Log.e(TAG, "Message is empty");
                    showEmptyErrorAlert();
                    return;
                }

                if(!track.isEmpty()) {
                    // Update Track info
                    musicTrack.setText("");
                    MusicService.getInstance().sendTrack(track);
                }

                if(!artist.isEmpty()) {
                    // Update Artist info
                    musicArtist.setText("");
                    MusicService.getInstance().sendArtist(artist);
                }

                if(!album.isEmpty()) {
                    // Update Album info
                    musicAlbum.setText("");
                    MusicService.getInstance().sendAlbum(album);
                }
                MusicService.getInstance().sendStatus(true);

                showToast("Sending music information");
            }
        });
    }

    private void getViewObjects() {
        manufacturer = (TextView) findViewById(R.id.txt_manufacturer);
        model = (TextView) findViewById(R.id.txt_model);
        serial = (TextView) findViewById(R.id.txt_serial);
        fw_revision = (TextView) findViewById(R.id.txt_fw_revision);
        hw_revision = (TextView) findViewById(R.id.txt_hw_revision);
        sw_revision = (TextView) findViewById(R.id.txt_sw_revision);

        alertMessage = (EditText) findViewById(R.id.input_alert);
        musicTrack = (EditText) findViewById(R.id.input_track);
        musicArtist = (EditText) findViewById(R.id.input_artist);
        musicAlbum = (EditText) findViewById(R.id.input_album);

        disconnectButton = (Button) findViewById(R.id.button_disconnect);
        sendAlert = (Button) findViewById(R.id.button_alert);
        sendMusicInfo = (Button) findViewById(R.id.button_music);
    }

    public void updateDeviceInformation() {
        manufacturer.setText("Manufacturer: " + deviceInformationService.mManufacturer);
        model.setText("Model Number: " + deviceInformationService.mModel);
        serial.setText("Serial Number: " + deviceInformationService.mSerial);
        fw_revision.setText("FW Revision : " + deviceInformationService.mFw_revision);
        hw_revision.setText("HW Revision: " + deviceInformationService.mHw_revision);
        sw_revision.setText("SW Revision: " + deviceInformationService.mSw_revision);
    }

    private void showEmptyErrorAlert() {
        new AlertDialog.Builder(this).setTitle("Field(s) empty").setMessage("Please specify a message").setNeutralButton("OK", null).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void enableDisableUI(boolean isEnabled) {
        manufacturer.setEnabled(isEnabled);
        model.setEnabled(isEnabled);
        serial.setEnabled(isEnabled);
        fw_revision.setEnabled(isEnabled);
        hw_revision.setEnabled(isEnabled);
        sw_revision.setEnabled(isEnabled);

        alertMessage.setEnabled(isEnabled);
        musicTrack.setEnabled(isEnabled);
        musicArtist.setEnabled(isEnabled);
        musicAlbum.setEnabled(isEnabled);

        disconnectButton.setEnabled(isEnabled);
        sendAlert.setEnabled(isEnabled);
        sendMusicInfo.setEnabled(isEnabled);
    }

    @Override
    public void onDestroy() {
        BluetoothService.getInstance().teardown();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!BluetoothService.getInstance().isConnected()) {
            enableDisableUI(false);

            MAC_Address = database.readMACFromDatabase();
            if(MAC_Address.isEmpty()) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
            } else {
                showToast("Attempting to connect to:\n" + MAC_Address);
                BluetoothService.getInstance().init(this);
                BluetoothService.getInstance().connect(MAC_Address);
                enableDisableUI(true);
            }
        } else {
            enableDisableUI(true);
        }
    }
}
