package com.example.infinitimeapp;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.listeners.IncomingCallReceiver;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static com.example.infinitimeapp.common.Constants.DELAY;
import static com.example.infinitimeapp.common.Constants.TAG;

public class WatchActivity extends AppCompatActivity implements NotificationService.NotificationListener,
                                                                SpotifyBroadcastReceiver.ReceiverListener,
                                                                UpdateUiListener.StatusChangedListener,
                                                                IncomingCallReceiver.CallReceiverListener {
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

    Button mButtonForgetDevice;
    Button mButtonSendAlert;
    Button mButtonSendMusicInfo;
    Switch mButtonSendPlayingState;

    DeviceInformationService mDeviceInformationService = DeviceInformationService.getsInstance();
    AlertNotificationService mAlertNotificationService = AlertNotificationService.getInstance();

    DatabaseConnection mDatabaseConnection;
    BluetoothService mBluetoothService;
    SpotifyConnection mSpotifyConnection;

    Handler handler = new Handler();;
    Runnable runnable;

    static final int REQUEST_ENABLE_BT = 1;
    public static String MAC_Address = "";

    public static TelecomManager sTelecomManager;

    private boolean mAutoConnect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_watch);

        mAutoConnect = true;
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

        final TelecomManager telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null && ContextCompat.checkSelfPermission(this, ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "WatchACtivity telecom!");
            sTelecomManager = telecomManager;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ANSWER_PHONE_CALLS}, 0);
        }

        new NotificationService().setListener(this);
        new SpotifyBroadcastReceiver().setListener(this);
        new IncomingCallReceiver().setListener(this);

        mSpotifyConnection = new SpotifyConnection();

        getViewObjects();
        applyButtonClickListers();

        makeBluetoothConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Log.d(TAG, "onStart");

        if(mSpotifyConnection != null && !mSpotifyConnection.isConnected()) {
            mSpotifyConnection.teardown();
            mSpotifyConnection.connect(this);
            MusicService.getInstance().useSpotifyConnection(mSpotifyConnection);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        teardown();
        if(mSpotifyConnection != null) {
            mSpotifyConnection.teardown();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.d(TAG, "onStop");

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
        MusicService.getInstance().subscribeOnEvents(mBluetoothService);

        AlertNotificationService.getInstance().subscribeOnEvents(mBluetoothService);

        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "RUNNING");
                if(mBluetoothService != null) {
                    CurrentTimeService.getInstance().updateTime(mBluetoothService);
                    handler.postDelayed(runnable, DELAY);
                }
            }
        }, DELAY);
    }

    private void applyButtonClickListers() {
        mButtonForgetDevice.setOnClickListener(v -> {
            showToast("Disconnecting from watch");
            mAutoConnect = false;
            teardown();
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

        mButtonForgetDevice = findViewById(R.id.button_forget_device);
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

        mButtonForgetDevice.setEnabled(isEnabled);
        mButtonSendAlert.setEnabled(isEnabled);
        mButtonSendMusicInfo.setEnabled(isEnabled);
    }

    @Override
    public void sendNotificationToWatch(String message) {
        AlertNotificationService.getInstance().sendMessage(mBluetoothService, message);
    }

    private void checkNotificationPermissions() {
        // Check permission for notification access
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Got permission");
        }

        String enabledNotificationListeners = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains("com.example.infinitimeapp.listeners.NotificationService")) {
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

            if (mAutoConnect) {
                makeBluetoothConnection();
            }
        }
    }

    @Override
    public void onUpdateUI() {
        updateDeviceInformation();
    }

    @Override
    public void onSpotifyConnectionChange(boolean isConnected) {}

    @Override
    public void onCallReceived(String incomingNumber) {
        AlertNotificationService.getInstance().sendMessage(mBluetoothService, incomingNumber, AlertNotificationService.ALERT_INCOMING_CALL);
    }

    @Override
    public void onCallOffHook(String incomingNumber) {
        AlertNotificationService.getInstance().sendMessage(mBluetoothService, incomingNumber, AlertNotificationService.ALERT_MISSED_CALL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0) {
            boolean AnswerPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (AnswerPermission) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
