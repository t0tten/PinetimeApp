package com.example.infinitimeapp.services;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurrentTimeService implements PinetimeService {
    public static final String CURRENT_TIME = "CURRENT_TIME";
    private static final Map<String, String> DEVICE_INFO_CHAR_MAP = Stream.of(new String[][]{
            {CURRENT_TIME, "00002a2b-0000-1000-8000-00805f9b34fb"}
    }).collect(Collectors.toMap(p -> p[0], p -> p[1]));

    @Override
    public String getCharacteristicName(UUID characteristicUUID) {
        return CURRENT_TIME;
    }

    @Override
    public UUID getCharacteristicUUID(String characteristicName) {
        return UUID.fromString(DEVICE_INFO_CHAR_MAP.get(characteristicName));
    }
}
