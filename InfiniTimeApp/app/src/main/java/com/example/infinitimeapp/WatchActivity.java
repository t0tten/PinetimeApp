package com.example.infinitimeapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.common.NotificationService;
import com.example.infinitimeapp.common.BroadcastReceiver;
import com.example.infinitimeapp.common.SpotifyConnection;
import com.example.infinitimeapp.common.Utils;
import com.example.infinitimeapp.database.Database;
import com.example.infinitimeapp.graphics.StatusChanged;
import com.example.infinitimeapp.services.AlertNotificationService;
import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.MusicService;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.infinitimeapp.common.Constants.TAG;

public class WatchActivity extends AppCompatActivity implements NotificationService.NotificationListener,
                                                                BroadcastReceiver.ReceiverListener,
                                                                StatusChanged.StatusChangedListener {
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

    Database database = new Database(this);
    SpotifyConnection mSpotifyConnection;
    BluetoothService mBluetoothService;

    static final int REQUEST_ENABLE_BT = 1;
    public static String MAC_Address = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_watch);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        checkNotificationPermissions();

        new NotificationService().setListener(this);
        new BroadcastReceiver().setListener(this);
        StatusChanged.getInstance().setListener(this);

        getViewObjects();
        applyButtonClickListers();
    }

    private void applyButtonClickListers() {
        disconnectButton.setOnClickListener(v -> {
            mBluetoothService.teardown();
            showToast("Disconnecting from watch");
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
            alertNotificationService.sendMessage(mBluetoothService, message);
        });

        sendMusicInfo.setOnClickListener(v -> {
            String track = musicTrack.getText().toString();
            String artist = musicArtist.getText().toString();
            String album = musicAlbum.getText().toString();
            boolean isPlaying = playingButton.isChecked();

            if(!track.isEmpty()) {
                musicTrack.setText("");
                MusicService.getInstance().sendTrack(mBluetoothService, track);
            }

            if(!artist.isEmpty()) {
                musicArtist.setText("");
                MusicService.getInstance().sendArtist(mBluetoothService, artist);
            }

            if(!album.isEmpty()) {
                musicAlbum.setText("");
                MusicService.getInstance().sendAlbum(mBluetoothService, album);
            }

            MusicService.getInstance().sendStatus(mBluetoothService, isPlaying);
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
        runOnUiThread(() -> {
            manufacturer.setText(String.format("%s %s", getString(R.string.manufacturer), deviceInformationService.mManufacturer));
            model.setText(String.format("%s %s", getString(R.string.model), deviceInformationService.mModel));
            serial.setText(String.format("%s %s", getString(R.string.serial), deviceInformationService.mSerial));
            fw_revision.setText(String.format("%s %s", getString(R.string.fw_revision), deviceInformationService.mFw_revision));
            hw_revision.setText(String.format("%s %s", getString(R.string.hw_revision), deviceInformationService.mHw_revision));
            sw_revision.setText(String.format("%s %s", getString(R.string.sw_revision), deviceInformationService.mSw_revision));
        });
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
        if(mBluetoothService.isConnected()) {
            mBluetoothService.teardown();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "UpdateUI");
        if(mBluetoothService == null) {
            MAC_Address = database.readMACFromDatabase();
            if(MAC_Address.isEmpty()) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
            } else {
                showToast("Attempting to connect to:\n" + MAC_Address);
                mBluetoothService = new BluetoothService(this);
                mBluetoothService.connect(MAC_Address);
            }
        }

        /*mSpotifyConnection = new SpotifyConnection(this);
        MusicService.getInstance().useSpotifyConnection(mSpotifyConnection);*/
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mSpotifyConnection.teardown();
    }

    @Override
    public void sendNotificationToWatch(String message) {
        //AlertNotificationService.getInstance().sendMessage(message);
    }

    private void checkNotificationPermissions() {
        // Check permission for notification access
        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains(getPackageName())) {
            new AlertDialog.Builder(this)
                    .setTitle("Need permission")
                    .setMessage("If you want to send notifications to device we need notification access.")
                    .setNeutralButton("OK", null)
                    .show();

            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }

    @Override
    public void onBroadcastReceive(String action) {
        if(action.equals("newTackInformation")) {
            Log.d(TAG, "onNotifyReceive - Action: " + action);
            /*SpotifyConnection.TrackInformation trackInformation = mSpotifyConnection.getTrackInformation();
            MusicService.getInstance().sendArtist(trackInformation.getArtist());
            MusicService.getInstance().sendTrack(trackInformation.getTrack());
            MusicService.getInstance().sendAlbum(trackInformation.getAlbum());*/
        }
    }

    @Override
    public void onConnectionChanged(boolean isConnected, BluetoothService bluetoothService) {
        if(isConnected) {
            mBluetoothService = bluetoothService;
            enableDisableUI(true);
            Utils.init(mBluetoothService);
        } else {
            enableDisableUI(false);
        }
    }

    @Override
    public void updateUI() {
        updateDeviceInformation();
    }
}
