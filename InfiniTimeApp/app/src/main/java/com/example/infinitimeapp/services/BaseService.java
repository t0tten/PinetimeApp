package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseService implements PinetimeService {
    private Map<String, String> mCharacteristics;

    public BaseService(Map<String, String> characteristics) {
        mCharacteristics = characteristics;
    }

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        Optional<String> key = mCharacteristics.entrySet().stream()
                .filter(e -> e.getValue().equals(characteristicUUID.toString()))
                .map(Map.Entry::getKey)
                .findFirst();
        return (key.get() == null) ? "" : key.get();
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(mCharacteristics.get(characteristicName));
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {

    }

    protected void read(BluetoothService bluetoothService, UUID characteristicUUID) {
        bluetoothService.read(characteristicUUID, this);
    }

    protected void write(BluetoothService bluetoothService, UUID characteristicUUID, byte[] message) {
        bluetoothService.write(characteristicUUID, message);
    }
}
