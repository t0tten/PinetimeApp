package com.example.infinitimeapp.services;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceInformationService implements PinetimeService {
    public static final UUID SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    public static final String MANUFACTURER = "MANUFACTURER";
    public static final String MODEL = "MODEL";
    public static final String SERIAL = "SERIAL";
    public static final String FW_REVISION_ID = "FW_REVISION_ID";
    public static final String HW_REVISION_ID = "HW_REVISION_ID";
    public static final String SW_REVISION_ID = "SW_REVISION_ID";

    private static final Map<String, String> DEVICE_INFO_CHAR_MAP = Stream.of(new String[][]{
            {MANUFACTURER, "00002a29-0000-1000-8000-00805f9b34fb"},
            {MODEL, "00002a24-0000-1000-8000-00805f9b34fb"},
            {SERIAL, "00002a25-0000-1000-8000-00805f9b34fb"},
            {FW_REVISION_ID, "00002a26-0000-1000-8000-00805f9b34fb"},
            {HW_REVISION_ID, "00002a27-0000-1000-8000-00805f9b34fb"},
            {SW_REVISION_ID, "00002a28-0000-1000-8000-00805f9b34fb"}
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        Optional<String> key = DEVICE_INFO_CHAR_MAP.entrySet().stream()
                .filter(e -> e.getValue().equals(characteristicUUID.toString()))
                .map(Map.Entry::getKey)
                .findFirst();
        return key.get();
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(DEVICE_INFO_CHAR_MAP.get(characteristicName));
    }
}
