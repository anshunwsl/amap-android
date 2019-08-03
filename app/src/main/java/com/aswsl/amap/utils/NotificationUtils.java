package com.aswsl.amap.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import com.aswsl.amap.AlarmMainActivity;
import com.aswsl.amap.MainActivity;

public class NotificationUtils extends ContextWrapper {

    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "com.dse.app";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    public NotificationUtils(Context base) {
        super(base);
        createChannels();
    }

    public void createChannels() {

        // create android channel
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //
            NotificationChannel androidChannel = new NotificationChannel(ANDROID_CHANNEL_ID,
                    ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            // Sets whether notifications posted to this channel should display notification lights
            androidChannel.enableLights(true);
            // Sets whether notification posted to this channel should vibrate.
            androidChannel.enableVibration(true);
            // Sets the notification light color for notifications posted to this channel
            androidChannel.setLightColor(Color.GREEN);
            // Sets whether notifications posted to this channel appear on the lockscreen or not
            androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            //
            androidChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            getManager().createNotificationChannel(androidChannel);
        }

    }

    private NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public Notification.Builder getAndroidChannelNotification(String title, String body) {
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //
            Intent intent = new Intent(this, AlarmMainActivity.class);
            //
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return new Notification.Builder(getApplicationContext(), ANDROID_CHANNEL_ID)
                    .setContentIntent(PendingIntent.
                            getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setContentTitle(title)
                    .setContentText(body)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setWhen(System.currentTimeMillis())
                    .setOngoing(true)
                    .setAutoCancel(true);
        } else {
            //
            return null;
        }

    }
}