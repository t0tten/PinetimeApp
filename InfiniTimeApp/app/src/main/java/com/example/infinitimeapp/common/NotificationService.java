package com.example.infinitimeapp.common;

import android.content.Context;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class NotificationService extends NotificationListenerService {
    public interface NotificationListener {
        void sendNotificationToWatch(String message);
    }

    Context context;
    static NotificationListener mNotificationListener;

    // TODO: Should ask user what kind of notifications they want to receive
    ArrayList<String> packageFilter = new ArrayList<>(List.of("com.google.android.apps.messaging",
                                                              "com.snapchat.android",
                                                              "com.facebook.orca"));

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.d(Constants.TAG, "ID :" + sbn.getId() + " \t " + sbn.getNotification().tickerText + " \t " + sbn.getPackageName()) ;
        if(packageFilter.contains(sbn.getPackageName()) && sbn.getNotification().tickerText != null) {
            mNotificationListener.sendNotificationToWatch(sbn.getNotification().tickerText.toString());
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

    public void setListener(NotificationListener mNotificationListener) {
        NotificationService.mNotificationListener = mNotificationListener ;
    }
}
