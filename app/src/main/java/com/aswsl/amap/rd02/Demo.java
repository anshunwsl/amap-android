package com.aswsl.amap.rd02;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class Demo extends Activity implements AMapLocationListener {


    private AlarmManager am;
    private PendingIntent pi;
    private Mreceiver mreceiver;
    private PowerManager.WakeLock wl = null;
    LocationReceiver locationReceiver;
    //
    private  String TAG=Demo.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
// TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //

        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.setReferenceCounted(false);
        //注册锁屏广播，主要是解决高德在锁屏黑屏情况下定位不更新的问题
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(Intent.ACTION_SCREEN_ON);
        intentfilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentfilter.addAction(Intent.ACTION_USER_PRESENT);
        mreceiver = new Mreceiver();
        registerReceiver(mreceiver, intentfilter);
        //注册设置定时唤醒定位

        IntentFilter intentFile = new IntentFilter();
        intentFile.addAction("repeating");
        locationReceiver = new LocationReceiver();
        registerReceiver(locationReceiver, intentFile);

//写一个定时的Pendingintent
        Intent intent = new Intent();
        intent.setAction("repeating");
        pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
    }


    private void wake() {
    // TODO Auto-generated method stub
    //每2秒激活广播，发起一次定位
    // startLocation(true);
        wl.acquire();
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis(), 2000, pi);
    }

    class LocationReceiver extends BroadcastReceiver {

        @Override


        public void onReceive(Context context, Intent intent) {
            Log.d("sunlei", "定位重新获取");
            //在这里重新申请定位

            //locationClient.startLocation();

        }


    }


    public class Mreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            //开屏
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                Log.d("sunlei", "开屏");
            }
            //锁屏
            else if (intent.ACTION_SCREEN_OFF.equals(action)) {
                Log.d("sunlei", "锁屏");
                //如果锁屏关闭当前常规定位方法，调用alarm,每2秒发动一次单次定位
                //locationClient.stopLocation();
                wake();

            }//解锁
            else if (intent.ACTION_USER_PRESENT.equals(action)) {
                Log.d("sunlei", "解锁");
                //am.cancel(pi);
            }
        }

    }

    @Override
    public void onLocationChanged(AMapLocation arg0) {
// TODO Auto-generated method stub

        //
        Log.d(TAG,"lon="+arg0.getLongitude());
    }


}