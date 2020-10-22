package com.example.infinitimeapp.models;

public class TrackInformation {
    String mArtist;
    String mTrack;
    String mAlbum;

    private TrackInformation(Builder builder) {
        mArtist = builder.mArtist;
        mTrack = builder.mTrack;
        mAlbum = builder.mTrack;
    }
    public String getArtist() { return mArtist; }
    public String getTrack() { return mTrack; }
    public String getAlbum() { return mAlbum; }

    public interface ArtistStep {
        TrackStep withArtist(String artist);
    }

    public interface TrackStep {
        AlbumStep withTrack(String track);
    }

    public interface AlbumStep {
        Build withAlbum(String album);
    }

    public interface Build {
        TrackInformation build();
    }

    public static class Builder implements ArtistStep, TrackStep, AlbumStep, Build {
        String mArtist;
        String mTrack;
        String mAlbum;

        @Override
        public TrackStep withArtist(String artist) {
            mArtist = artist;
            return this;
        }

        @Override
        public AlbumStep withTrack(String track) {
            mTrack = track;
            return this;
        }

        @Override
        public Build withAlbum(String album) {
            mAlbum = album;
            return this;
        }

        @Override
        public TrackInformation build() {
            return new TrackInformation(this);
        }
    }
};
