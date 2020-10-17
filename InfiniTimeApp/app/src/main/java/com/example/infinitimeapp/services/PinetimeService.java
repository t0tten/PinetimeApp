package com.example.infinitimeapp.services;

import java.util.UUID;

public interface PinetimeService {
    String getCharacteristicName(UUID characteristicUUID);
    UUID getCharacteristicUUID(String characteristicName);
    void onDataRecieved(UUID characteristicName, byte[] message);
}
