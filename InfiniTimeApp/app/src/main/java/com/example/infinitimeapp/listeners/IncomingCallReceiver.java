package com.example.infinitimeapp.listeners;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import static com.example.infinitimeapp.common.Constants.*;

public class IncomingCallReceiver extends android.content.BroadcastReceiver {
    public interface CallReceiverListener {
        void onCallReceived(String incomingNumber);
        void onCallOffHook(String incomingNumber);
    }

    private static CallReceiverListener mCallReceiverListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new CustomPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);

        Bundle bundle = intent.getExtras();
        String incomingNumber = bundle.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String callerName = "";
        if(incomingNumber != null) {
            callerName = getContactName(incomingNumber, context);
            Log.d(TAG, "Incoming number: " + incomingNumber);
            Log.d(TAG, "Name: " + callerName);

            final String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                if (callerName != null && !callerName.isEmpty()) {
                    mCallReceiverListener.onCallReceived(callerName);
                } else {
                    mCallReceiverListener.onCallReceived(incomingNumber);
                }
            }

            if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                if (callerName != null && !callerName.isEmpty()) {
                    mCallReceiverListener.onCallOffHook(callerName);
                } else {
                    mCallReceiverListener.onCallOffHook(incomingNumber);
                }
            }
        }
    }

    private String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        return contactName;
    }

    public void setListener(CallReceiverListener mReceiverListener) {
        IncomingCallReceiver.mCallReceiverListener = mReceiverListener;
    }

    public static class CustomPhoneStateListener extends PhoneStateListener {

        public void onCallStateChanged(int state, String incomingNumber) {
        }
    }
}
