package com.example.infinitimeapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.common.NotificationService;
import com.example.infinitimeapp.database.Database;
import com.example.infinitimeapp.services.AlertNotificationService;
import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.MusicService;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.infinitimeapp.common.Constants.DELAY_IN_MILLIS;
import static com.example.infinitimeapp.common.Constants.TAG;

public class WatchActivity extends AppCompatActivity implements NotificationService.NotificationListener {
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
    Switch playingButton;

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
        checkPermissionsOrAsk();

        new NotificationService().setListener(this);

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
        disconnectButton.setOnClickListener(v -> {
            BluetoothService.getInstance().teardown();
            showToast("Disconnecting from watch");
            connectionState = false;
            enableDisableUI(false);
            database.removeMacFromDatabase();
        });

        sendAlert.setOnClickListener(v -> {
            String message = alertMessage.getText().toString();
            if(message.isEmpty()) {
                Log.e(TAG, "Message is empty");
                showEmptyErrorAlert();
                return;
            }
            showToast("Sending alert: " + message);
            alertMessage.setText("");
            alertNotificationService.sendMessage(message);
        });

        sendMusicInfo.setOnClickListener(v -> {
            String track = musicTrack.getText().toString();
            String artist = musicArtist.getText().toString();
            String album = musicAlbum.getText().toString();
            boolean isPlaying = playingButton.isChecked();

            if(!track.isEmpty()) {
                musicTrack.setText("");
                MusicService.getInstance().sendTrack(track);
            }

            if(!artist.isEmpty()) {
                musicArtist.setText("");
                MusicService.getInstance().sendArtist(artist);
            }

            if(!album.isEmpty()) {
                musicAlbum.setText("");
                MusicService.getInstance().sendAlbum(album);
            }

            MusicService.getInstance().sendStatus(isPlaying);
            showToast("Sending music information");
        });
    }

    private void getViewObjects() {
        manufacturer = findViewById(R.id.txt_manufacturer);
        model = findViewById(R.id.txt_model);
        serial = findViewById(R.id.txt_serial);
        fw_revision = findViewById(R.id.txt_fw_revision);
        hw_revision = findViewById(R.id.txt_hw_revision);
        sw_revision = findViewById(R.id.txt_sw_revision);

        alertMessage = findViewById(R.id.input_alert);
        musicTrack = findViewById(R.id.input_track);
        musicArtist = findViewById(R.id.input_artist);
        musicAlbum = findViewById(R.id.input_album);

        disconnectButton = findViewById(R.id.button_disconnect);
        sendAlert = findViewById(R.id.button_alert);
        sendMusicInfo = findViewById(R.id.button_music);
        playingButton = findViewById(R.id.button_playing);
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
        new AlertDialog.Builder(this)
                .setTitle("Field(s) empty")
                .setMessage("Please specify a message")
                .setNeutralButton("OK", null)
                .show();
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

    @Override
    public void sendMessageToWatch(String message) {
        //AlertNotificationService.getInstance().sendMessage(message);
    }

    private void checkPermissionsOrAsk() {
        // Check permission for notification access
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }
}
