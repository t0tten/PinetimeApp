package com.example.infinitimeapp.services;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicService extends BaseService {
    private static final String EVENT = "EVENT";
    private static final String STATUS = "STATUS";
    private static final String TRACK = "TRACK";
    private static final String ARTIST = "ARTIST";
    private static final String ALBUM = "ALBUM";

    private static final char EVENT_MUSIC_OPEN = 0xe0;
    private static final char EVENT_MUSIC_PLAY = 0x00;
    private static final char EVENT_MUSIC_PAUSE = 0x01;
    private static final char EVENT_MUSIC_NEXT = 0x03;
    private static final char EVENT_MUSIC_PREV = 0x04;
    private static final char EVENT_MUSIC_VOLUP = 0x05;
    private static final char EVENT_MUSIC_VOLDOWN = 0x06;

    private static final char STATUS_MUSIC_PAUSED = 0x00;
    private static final char STATUS_MUSIC_PLAYING = 0x01;

    public MusicService() {
        CHAR_MAP = Stream.of(new String[][]{
                {EVENT, "c7e50002-00fc-48fe-8e23-433b3a1942d0"},
                {STATUS, "c7e50003-00fc-48fe-8e23-433b3a1942d0"},
                {TRACK, "c7e50005-00fc-48fe-8e23-433b3a1942d0"},
                {ARTIST, "c7e50004-00fc-48fe-8e23-433b3a1942d0"},
                {ALBUM, "c7e50006-00fc-48fe-8e23-433b3a1942d0"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    @Override
    public void onDataRecieved(UUID characteristicName, byte[] message) {
        switch(getCharacteristicName(characteristicName)) {
            case EVENT:
                eventHandler(message);
                break;
            case STATUS:
                break;
            case TRACK:
                break;
            case ARTIST:
                break;
            case ALBUM:
                break;
            default:
        }
    }

    private void eventHandler(byte[] message) {
        char test = 'h';
        switch(test) {
            case EVENT_MUSIC_OPEN:
                break;
            case EVENT_MUSIC_PLAY:
                break;
            case EVENT_MUSIC_PAUSE:
                break;
            case EVENT_MUSIC_NEXT:
                break;
            case EVENT_MUSIC_PREV:
                break;
            case EVENT_MUSIC_VOLUP:
                break;
            case EVENT_MUSIC_VOLDOWN:
                break;
        }
    }
}
