package com.example.infinitimeapp.models;

public class BluetoothDevice {
    private String mName;
    private String mMac;

    private BluetoothDevice(Builder builder) {
        mName = builder.mName;
        mMac = builder.mMac;
    }
    public String getName() { return mName; }
    public String getMac() { return mMac; }

    public interface NameStep {
        MacStep withName(String name);
    }

    public interface MacStep {
        Build withMac(String mac);
    }

    public interface Build {
        BluetoothDevice build();
    }

    public static class Builder implements NameStep, MacStep, Build {
        String mName;
        String mMac;

        @Override
        public MacStep withName(String name) {
            mName = name;
            return this;
        }

        @Override
        public Build withMac(String mac) {
            if(mName == null) throw new NullPointerException("Name is null");
            mMac = mac;
            return this;
        }

        @Override
        public BluetoothDevice build() {
            return new BluetoothDevice(this);
        }
    }
}
