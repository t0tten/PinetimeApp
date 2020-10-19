package com.example.infinitimeapp.common;

import android.content.Context;
import android.content.Intent;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    public interface ReceiverListener {
        void onNotifyReceive(String action);
    }

    static ReceiverListener mReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        mReceiverListener.onNotifyReceive(intent.getAction());
    }

    public void setListener(ReceiverListener mReceiverListener) {
        BroadcastReceiver.mReceiverListener = mReceiverListener ;
    }
}
