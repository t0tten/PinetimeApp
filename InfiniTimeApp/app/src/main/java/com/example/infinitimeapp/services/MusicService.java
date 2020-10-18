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
    private static final char EVENT_MUSIC_VOLUME_UP = 0x05;
    private static final char EVENT_MUSIC_VOLUME_DOWN = 0x06;

    private static MusicService sInstance = null;

    private MusicService() {
        CHAR_MAP = Stream.of(new String[][]{
                {EVENT, "c7e50002-00fc-48fe-8e23-433b3a1942d0"},
                {STATUS, "c7e50003-00fc-48fe-8e23-433b3a1942d0"},
                {TRACK, "c7e50005-00fc-48fe-8e23-433b3a1942d0"},
                {ARTIST, "c7e50004-00fc-48fe-8e23-433b3a1942d0"},
                {ALBUM, "c7e50006-00fc-48fe-8e23-433b3a1942d0"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    public static MusicService getInstance() {
        if (sInstance == null)
            sInstance = new MusicService();

        return sInstance;
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
        switch((char) message[0]) {
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
            case EVENT_MUSIC_VOLUME_UP:
                break;
            case EVENT_MUSIC_VOLUME_DOWN:
                break;
        }
    }

    public void sendTrack(String track) {
        write(getCharacteristicUUID(TRACK), track.getBytes());
    }

    public void sendArtist(String artist) {
        write(getCharacteristicUUID(ARTIST), artist.getBytes());
    }

    public void sendAlbum(String album) {
        write(getCharacteristicUUID(ALBUM), album.getBytes());
    }

    public void sendStatus(boolean isPlaying) {
        byte[] message = new byte[2];
        message[0] = (byte)(isPlaying ? 1 : 0);
        message[1] = '\0';
        write(getCharacteristicUUID(STATUS), message);
    }
}
