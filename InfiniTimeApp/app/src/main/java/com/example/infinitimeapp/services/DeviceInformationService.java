package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.listeners.UpdateUiListener;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceInformationService extends BaseService {
    private static final String MANUFACTURER = "MANUFACTURER";
    private static final String MODEL = "MODEL";
    private static final String SERIAL = "SERIAL";
    private static final String FW_REVISION_ID = "FW_REVISION_ID";
    private static final String HW_REVISION_ID = "HW_REVISION_ID";
    private static final String SW_REVISION_ID = "SW_REVISION_ID";

    private static DeviceInformationService sInstance;

    public String mManufacturer = "";
    public String mModel = "";
    public String mSerial = "";
    public String mFw_revision = "";
    public String mHw_revision = "";
    public String mSw_revision = "";

    private DeviceInformationService() {
        super(Stream.of(new String[][]{
                {MANUFACTURER, "00002a29-0000-1000-8000-00805f9b34fb"},
                {MODEL, "00002a24-0000-1000-8000-00805f9b34fb"},
                {SERIAL, "00002a25-0000-1000-8000-00805f9b34fb"},
                {FW_REVISION_ID, "00002a26-0000-1000-8000-00805f9b34fb"},
                {HW_REVISION_ID, "00002a27-0000-1000-8000-00805f9b34fb"},
                {SW_REVISION_ID, "00002a28-0000-1000-8000-00805f9b34fb"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1])));
    }

    public static DeviceInformationService getsInstance() {
        if (sInstance == null) sInstance = new DeviceInformationService();
        return sInstance;
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case MANUFACTURER:
                mManufacturer = new String(message);
                break;
            case MODEL:
                mModel = new String(message);
                break;
            case SERIAL:
                mSerial = new String(message);
                break;
            case FW_REVISION_ID:
                mFw_revision = new String(message);
                break;
            case HW_REVISION_ID:
                mHw_revision = new String(message);
                break;
            case SW_REVISION_ID:
                mSw_revision = new String(message);
                break;
            default:
        }
        if(!mManufacturer.isEmpty() &&
            !mModel.isEmpty() &&
            !mSerial.isEmpty() &&
            !mFw_revision.isEmpty() &&
            !mHw_revision.isEmpty() &&
            !mSw_revision.isEmpty()) {
            UpdateUiListener.getInstance().getListener().onUpdateUI();
        }
    }

    public void getManufaturer(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(MANUFACTURER));
    }

    public void getModel(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(MODEL));
    }

    public void getSerial(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(SERIAL));
    }

    public void getFwRevisionId(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(FW_REVISION_ID));
    }

    public void getHwRevisionId(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(HW_REVISION_ID));
    }

    public void getSwRevisionId(BluetoothService bluetoothService) {
        read(bluetoothService, getCharacteristicUUID(SW_REVISION_ID));
    }
}
