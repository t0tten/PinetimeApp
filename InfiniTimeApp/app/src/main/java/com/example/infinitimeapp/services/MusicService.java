package com.example.infinitimeapp.services;

import com.example.infinitimeapp.bluetooth.BluetoothService;
import com.example.infinitimeapp.utils.SpotifyConnection;
import com.example.infinitimeapp.models.TrackInformation;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MusicService extends BaseService {
    private static final String EVENT = "EVENT";
    private static final String STATUS = "STATUS";
    private static final String TRACK = "TRACK";
    private static final String ARTIST = "ARTIST";
    private static final String ALBUM = "ALBUM";

    private static MusicService sInstance;
    private SpotifyConnection mSpotifyConnection;

    private MusicService() {
        super(Stream.of(new String[][]{
                {EVENT, "c7e50002-00fc-48fe-8e23-433b3a1942d0"},
                {STATUS, "c7e50003-00fc-48fe-8e23-433b3a1942d0"},
                {TRACK, "c7e50005-00fc-48fe-8e23-433b3a1942d0"},
                {ARTIST, "c7e50004-00fc-48fe-8e23-433b3a1942d0"},
                {ALBUM, "c7e50006-00fc-48fe-8e23-433b3a1942d0"}
        }).collect(Collectors.toMap(p -> p[0], p -> p[1])));
    }

    public static MusicService getInstance() {
        if (sInstance == null) sInstance = new MusicService();
        return sInstance;
    }

    public UUID getEventUUID() {
        return getCharacteristicUUID(EVENT);
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
        if(mSpotifyConnection != null) {
            final char EVENT_MUSIC_PLAY = 0x00;
            final char EVENT_MUSIC_PAUSE = 0x01;
            final char EVENT_MUSIC_NEXT = 0x03;
            final char EVENT_MUSIC_PREV = 0x04;
            final char EVENT_MUSIC_VOLUME_UP = 0x05;
            final char EVENT_MUSIC_VOLUME_DOWN = 0x06;

            switch ((char) message[0]) {
                case EVENT_MUSIC_PLAY:
                    mSpotifyConnection.resume();
                    break;
                case EVENT_MUSIC_PAUSE:
                    mSpotifyConnection.pause();
                    break;
                case EVENT_MUSIC_NEXT:
                    mSpotifyConnection.nextTrack();
                    break;
                case EVENT_MUSIC_PREV:
                    mSpotifyConnection.previousTrack();
                    break;
                case EVENT_MUSIC_VOLUME_UP:
                    mSpotifyConnection.volumeUp();
                    break;
                case EVENT_MUSIC_VOLUME_DOWN:
                    mSpotifyConnection.volumeDown();
                    break;
            }
        }
    }

    private void sendTrack(BluetoothService bluetoothService, String track) {
        write(bluetoothService, getCharacteristicUUID(TRACK), track.getBytes());
    }

    private void sendArtist(BluetoothService bluetoothService, String artist) {
        write(bluetoothService, getCharacteristicUUID(ARTIST), artist.getBytes());
    }

    public void useSpotifyConnection(SpotifyConnection spotifyConnection) {
        mSpotifyConnection = spotifyConnection;
    }

    private void sendAlbum(BluetoothService bluetoothService, String album) {
        write(bluetoothService, getCharacteristicUUID(ALBUM), album.getBytes());
    }

    public void sendTrackInformation(BluetoothService bluetoothService, TrackInformation trackInformation) {
        sendArtist(bluetoothService, trackInformation.getArtist());
        sendTrack(bluetoothService, trackInformation.getTrack());
        sendAlbum(bluetoothService, trackInformation.getAlbum());
    }

    public void subscribeOnEvents(BluetoothService bluetoothService) {
        bluetoothService.listenOnCharacteristic(getCharacteristicUUID(EVENT));
    }

    public void sendStatus(BluetoothService bluetoothService, boolean isPlaying) {
        byte[] message = new byte[2];
        message[0] = (byte)(isPlaying ? 1 : 0);
        message[1] = '\0';
        write(bluetoothService, getCharacteristicUUID(STATUS), message);
    }
}
