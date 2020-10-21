package com.example.infinitimeapp.listeners;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;
import java.util.List;

public class NotificationService extends NotificationListenerService {
    public interface NotificationListener {
        void sendNotificationToWatch(String message);
    }

    Context mContext;
    static NotificationListener mNotificationListener;

    // TODO: Should ask user what kind of notifications they want to receive
    ArrayList<String> mPackageFilter = new ArrayList<>(List.of("com.google.android.apps.messaging",
                                                              "com.snapchat.android",
                                                              "com.facebook.orca"));

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //Log.d(Constants.TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName()) ;
        if(mPackageFilter.contains(sbn.getPackageName()) && sbn.getNotification().tickerText != null) {
            mNotificationListener.sendNotificationToWatch(sbn.getNotification().tickerText.toString());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

    public void setListener(NotificationListener mNotificationListener) {
        NotificationService.mNotificationListener = mNotificationListener ;
    }
}
