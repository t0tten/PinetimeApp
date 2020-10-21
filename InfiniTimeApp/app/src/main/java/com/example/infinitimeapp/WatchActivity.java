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
import com.example.infinitimeapp.listeners.NotificationService;
import com.example.infinitimeapp.listeners.SpotifyBroadcastReceiver;
import com.example.infinitimeapp.utils.DatabaseConnection;
import com.example.infinitimeapp.listeners.UpdateUiListener;
import com.example.infinitimeapp.models.TrackInformation;
import com.example.infinitimeapp.services.AlertNotificationService;
import com.example.infinitimeapp.services.CurrentTimeService;
import com.example.infinitimeapp.services.DeviceInformationService;
import com.example.infinitimeapp.services.MusicService;
import com.example.infinitimeapp.utils.SpotifyConnection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.infinitimeapp.common.Constants.TAG;

public class WatchActivity extends AppCompatActivity implements NotificationService.NotificationListener,
                                                                SpotifyBroadcastReceiver.ReceiverListener,
                                                                UpdateUiListener.StatusChangedListener {
    TextView mViewTextManufacturer;
    TextView mViewTextModel;
    TextView mViewTextSerial;
    TextView mViewTextFwRevision;
    TextView mViewTextHwRevision;
    TextView mViewTextSwRevision;

    EditText mViewEditAlertMessage;
    EditText mViewEditMusicTrack;
    EditText mViewEditMusicArtist;
    EditText mViewEditMusicAlbum;

    Button mButtonDisconnect;
    Button mButtonSendAlert;
    Button mButtonSendMusicInfo;
    Switch mButtonSendPlayingState;

    DeviceInformationService mDeviceInformationService = DeviceInformationService.getsInstance();
    AlertNotificationService mAlertNotificationService = AlertNotificationService.getInstance();

    DatabaseConnection mDatabaseConnection;
    BluetoothService mBluetoothService;
    SpotifyConnection mSpotifyConnection;

    static final int REQUEST_ENABLE_BT = 1;
    public static String MAC_Address = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate()");

        setContentView(R.layout.activity_watch);

        mDatabaseConnection = new DatabaseConnection(this);
        UpdateUiListener.getInstance().setListener(this);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        checkNotificationPermissions();

        new NotificationService().setListener(this);
        new SpotifyBroadcastReceiver().setListener(this);

        getViewObjects();
        applyButtonClickListers();

        makeBluetoothConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart");

        if(mSpotifyConnection == null) {
            mSpotifyConnection = new SpotifyConnection(this);
            MusicService.getInstance().useSpotifyConnection(mSpotifyConnection);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        teardown();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d(TAG, "onStop");
        if(mSpotifyConnection != null) {
            mSpotifyConnection.teardown();
        }
    }

    private void teardown() {
        if(mBluetoothService != null && mBluetoothService.isConnected()) {
            mBluetoothService.teardown();
        }
    }

    private void makeBluetoothConnection() {
        if(mBluetoothService == null) {
            MAC_Address = mDatabaseConnection.readMACFromDatabase();
            if(MAC_Address.isEmpty()) {
                Intent intent = new Intent(getApplicationContext(), ScanActivity.class);
                startActivity(intent);
            } else {
                showToast("Attempting to connect to:\n" + MAC_Address);
                mBluetoothService = new BluetoothService(this);
                mBluetoothService.connect(MAC_Address);
            }
        }
    }

    private void initWatch() {
        DeviceInformationService s = DeviceInformationService.getsInstance();
        s.getHwRevisionId(mBluetoothService);
        s.getFwRevisionId(mBluetoothService);
        s.getManufaturer(mBluetoothService);
        s.getSerial(mBluetoothService);
        s.getSwRevisionId(mBluetoothService);
        s.getModel(mBluetoothService);

        CurrentTimeService.getInstance().updateTime(mBluetoothService);
    }

    private void applyButtonClickListers() {
        mButtonDisconnect.setOnClickListener(v -> {
            mBluetoothService.teardown();
            showToast("Disconnecting from watch");
            mDatabaseConnection.removeMacFromDatabase();
        });

        mButtonSendAlert.setOnClickListener(v -> {
            String message = mViewEditAlertMessage.getText().toString();
            if(message.isEmpty()) {
                Log.e(TAG, "Message is empty");
                showEmptyErrorAlert();
                return;
            }
            showToast("Sending alert: " + message);
            mViewEditAlertMessage.setText("");
            mAlertNotificationService.sendMessage(mBluetoothService, message);
        });

        mButtonSendMusicInfo.setOnClickListener(v -> {
            String track = mViewEditMusicTrack.getText().toString();
            String artist = mViewEditMusicArtist.getText().toString();
            String album = mViewEditMusicAlbum.getText().toString();
            boolean isPlaying = mButtonSendPlayingState.isChecked();

            if(!track.isEmpty()) {
                mViewEditMusicTrack.setText("");
            }

            if(!artist.isEmpty()) {
                mViewEditMusicArtist.setText("");
            }

            if(!album.isEmpty()) {
                mViewEditMusicAlbum.setText("");
            }

            TrackInformation trackInformation = new TrackInformation.Builder()
                    .withArtist(artist)
                    .withTrack(track)
                    .withAlbum(album)
                    .build();
            MusicService.getInstance().sendTrackInformation(mBluetoothService, trackInformation);

            MusicService.getInstance().sendStatus(mBluetoothService, isPlaying);
            showToast("Sending music information");
        });
    }

    private void getViewObjects() {
        mViewTextManufacturer = findViewById(R.id.txt_manufacturer);
        mViewTextModel = findViewById(R.id.txt_model);
        mViewTextSerial = findViewById(R.id.txt_serial);
        mViewTextFwRevision = findViewById(R.id.txt_fw_revision);
        mViewTextHwRevision = findViewById(R.id.txt_hw_revision);
        mViewTextSwRevision = findViewById(R.id.txt_sw_revision);

        mViewEditAlertMessage = findViewById(R.id.input_alert);
        mViewEditMusicTrack = findViewById(R.id.input_track);
        mViewEditMusicArtist = findViewById(R.id.input_artist);
        mViewEditMusicAlbum = findViewById(R.id.input_album);

        mButtonDisconnect = findViewById(R.id.button_disconnect);
        mButtonSendAlert = findViewById(R.id.button_alert);
        mButtonSendMusicInfo = findViewById(R.id.button_music);
        mButtonSendPlayingState = findViewById(R.id.button_playing);
    }

    public void updateDeviceInformation() {
        runOnUiThread(() -> {
            mViewTextManufacturer.setText(String.format("%s %s", getString(R.string.manufacturer), mDeviceInformationService.mManufacturer));
            mViewTextModel.setText(String.format("%s %s", getString(R.string.model), mDeviceInformationService.mModel));
            mViewTextSerial.setText(String.format("%s %s", getString(R.string.serial), mDeviceInformationService.mSerial));
            mViewTextFwRevision.setText(String.format("%s %s", getString(R.string.fw_revision), mDeviceInformationService.mFw_revision));
            mViewTextHwRevision.setText(String.format("%s %s", getString(R.string.hw_revision), mDeviceInformationService.mHw_revision));
            mViewTextSwRevision.setText(String.format("%s %s", getString(R.string.sw_revision), mDeviceInformationService.mSw_revision));
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
        mViewTextManufacturer.setEnabled(isEnabled);
        mViewTextModel.setEnabled(isEnabled);
        mViewTextSerial.setEnabled(isEnabled);
        mViewTextFwRevision.setEnabled(isEnabled);
        mViewTextHwRevision.setEnabled(isEnabled);
        mViewTextSwRevision.setEnabled(isEnabled);

        mViewEditAlertMessage.setEnabled(isEnabled);
        mViewEditMusicTrack.setEnabled(isEnabled);
        mViewEditMusicArtist.setEnabled(isEnabled);
        mViewEditMusicAlbum.setEnabled(isEnabled);

        mButtonDisconnect.setEnabled(isEnabled);
        mButtonSendAlert.setEnabled(isEnabled);
        mButtonSendMusicInfo.setEnabled(isEnabled);
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
    public void onPlayingStateChanged(boolean isPlaying) {
        MusicService.getInstance().sendStatus(mBluetoothService, isPlaying);
    }

    @Override
    public void onTrackChanged(TrackInformation trackInformation) {
        MusicService.getInstance().sendTrackInformation(mBluetoothService, trackInformation);
    }

    @Override
    public void onConnectionChanged(boolean isConnected, BluetoothService bluetoothService) {
        if(isConnected) {
            if(mBluetoothService == null) {
                mBluetoothService = bluetoothService;
            }
            enableDisableUI(true);
            initWatch();
        } else {
            enableDisableUI(false);
        }
    }

    @Override
    public void onUpdateUI() {
        updateDeviceInformation();
    }

    @Override
    public void onSpotifyConnectionChange(boolean isConnected) {
        String message;
        if(!isConnected) {
            message = "Could not connect to Remote Spotify.";
            new AlertDialog.Builder(this)
                    .setTitle("Remote Spotify")
                    .setMessage(message)
                    .setNeutralButton("OK", null)
                    .show();
        } else {
            message = "Connected to Remote Spotify.\nYou can now control spotify from watch.";
            showToast(message);
        }
    }
}
