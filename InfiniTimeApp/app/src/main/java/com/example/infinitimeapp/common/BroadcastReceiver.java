package com.example.infinitimeapp.common;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BroadcastReceiver extends android.content.BroadcastReceiver {
    public interface ReceiverListener {
        void onBroadcastReceive(String action);
    }

    static ReceiverListener mReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(Constants.TAG, "ACTION: " + intent.getAction());
        mReceiverListener.onBroadcastReceive(intent.getAction());
    }

    public void setListener(ReceiverListener mReceiverListener) {
        BroadcastReceiver.mReceiverListener = mReceiverListener ;
    }
}
