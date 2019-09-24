package com.aswsl.amap.rd01;

import android.content.Context;
import android.content.Intent;

import androidx.legacy.content.WakefulBroadcastReceiver;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class WLWakefulReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //
        String extra = intent.getStringExtra("msg");

        //
        Intent serviceIntent = new Intent(context, MyIntentService.class);
        serviceIntent.putExtra("msg", extra);
        //

        //
        // STOPSHIP: 2019-09-24
        startWakefulService(context, serviceIntent);
    }
}