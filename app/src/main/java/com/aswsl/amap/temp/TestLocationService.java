package com.aswsl.amap.temp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
//import android.support.annotation.Nullable;

//import com.amap.api.maps2d.AMapUtils;
//import com.amap.api.maps2d.model.LatLng;
//import com.evhai.hyt.base.AppManager;
//import com.evhai.hyt.model.StaticModel;
//import com.evhai.hyt.singleton.MLocation;
//import com.evhai.hyt.singleton.UserInfo;
//import com.evhai.hyt.utils.LogUtil;
//import com.evhai.hyt.utils.dialog.DialogUtil;
//import com.evhai.hyt.view.main.MainActivity;

/**
 *
 */
public class TestLocationService extends Service {
    private boolean isFirst = true;
    private int num = 1;
//    private WaitCountDownTimer timer;

    @Override
    public void onCreate() {
        super.onCreate();
//        LogUtil.verbose("定位：onCreate");
        //
        Log.d(TAG,"定位：onCreate");

//        MLocation.getInstance().locationCallBack = locationCallBack;
//        startCount();
        //
        MLocation.getInstance().initOption(this.getApplicationContext());
        //
        startAlarm();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        LogUtil.verbose("定位：onStartCommand");
        Log.d(TAG,"定位：onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        new PollingThread().start();
    }

    //
    private String TAG=TestLocationService.class.getSimpleName();
    int count = 0;
    class PollingThread extends Thread {
        @Override
        public void run() {
            count ++;
            Log.d(TAG,"定位：PollingThread="+count);
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (timer != null) {
//            timer = null;
//            timer.cancel();
//        }
//        LogUtil.verbose("定位：onDestroy");

        //
        Log.d(TAG,"定位：onDestroy");
        startService(new Intent(TestLocationService.this, TestLocationService.class));
//        MLocation.getInstance().locationCallBack = null;
    }

//    /**
//     * 倒计时
//     */
//    public class WaitCountDownTimer extends CountDownTimer {
//
//        /**
//         * @param millisInFuture    倒计时长度
//         * @param countDownInterval 防重复点击间隔
//         */
//        public WaitCountDownTimer(long millisInFuture, long countDownInterval) {
//            super(millisInFuture, countDownInterval);
//        }
//
//        //计时过程
//        @Override
//        public void onTick(long l) {
//            LogUtil.error(num + "次倒计时进行中：" + l / 1000 + "s");
//        }
//
//        //计时完毕的方法
//        @Override
//        public void onFinish() {
//            LogUtil.error(num + "次倒计时完成");
//            StaticModel.updatePoint(AppManager.getAppManager().findActivity(MainActivity.class));
//            num++;
//            LogUtil.error(num + "次倒计时开始");
//            startCount();
//        }
//    }
//
//    private void startCount() {
//        timer = new WaitCountDownTimer(30 * 1000, 1000);
//        timer.start();
//    }

    /**
     * 防止后台2个小时后就休眠
     */
    public void startAlarm() {
//        LogUtil.verbose("定位：startAlarm");
        AlarmManager am;
        Intent intentAlarm;
        PendingIntent pendSender;
        //首先获得系统服务
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //设置闹钟的意图，我这里是去调用一个服务，该服务功能就是获取位置并且上传
        intentAlarm = new Intent(this, TestLocationService.class);
        pendSender = PendingIntent.getService(this, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
//        am.cancel(pendSender);
        //AlarmManager.RTC_WAKEUP ;这个参数表示系统会唤醒进程；设置的间隔时间是20分钟
        long triggerAtTime = System.currentTimeMillis() + 10 * 1000;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendSender);
//            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtTime,  1000, pendSender);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendSender);
//            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,  1000, pendSender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAtTime,
                    pendSender);
        }
    }
}
