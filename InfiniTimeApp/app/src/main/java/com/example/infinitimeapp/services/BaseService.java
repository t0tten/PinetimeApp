package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BaseService implements PinetimeService {
    protected static Map<String, String> CHAR_MAP;

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        Optional<String> key = CHAR_MAP.entrySet().stream()
                .filter(e -> e.getValue().equals(characteristicUUID.toString()))
                .map(Map.Entry::getKey)
                .findFirst();
        return key.get();
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(CHAR_MAP.get(characteristicName));
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {

    }

    protected void read(UUID characteristicUUID) {
        BluetoothService.getInstance().read(characteristicUUID, this);
    }

    protected void write(UUID characteristicUUID, byte[] message) {
        BluetoothService.getInstance().write(characteristicUUID, message);
    }

}
