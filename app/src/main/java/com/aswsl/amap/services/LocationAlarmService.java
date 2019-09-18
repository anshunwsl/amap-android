package com.aswsl.amap.services;



import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.aswsl.amap.R;
import com.aswsl.amap.events.LocationEvent;
import com.aswsl.amap.utils.ConstantUtils;
//import com.swq.mcsrefine.activity.R;
//import com.swq.mcsrefine.utils.ConstantUtils;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by agisv2 on 2019-08-03 13:54
 *
 * @author agisv2 wangsl@dse.cn
 * @version 1.0.0
 */


public class LocationAlarmService extends Service {

    public AMapLocationClient locationClient;
    //声明mLocationOption对象
    public AMapLocationClientOption locationOption = null;

    //android 8.0后台定位权限
    private static final String NOTIFICATION_CHANNEL_NAME = "Location";
    private NotificationManager notificationManager = null;
    boolean isCreateChannel = false;
    private  Intent locationIntent = new Intent();
    public LocationAlarmService() {
    }

    @Override
    public void onCreate() {
        initLocation();
        Log.e(TAG, "init service..");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocation();
        Log.e(TAG, "service restart success!");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyLocation();

    }

    /**
     * 销毁定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void destroyLocation() {
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.disableBackgroundLocation(true);
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(locationListener);
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }

    /**
     * 初始化定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void initLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        // 设置是否单次定位
        locationOption.setOnceLocation(false);
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationClient.enableBackgroundLocation(2004, buildNotification());
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

    }

    private void startLocation() {
        // 启动定位
        if (ConstantUtils.XUN_CHA_SERVICE) {
            startAlarm();
            locationClient.startLocation();
        } else {
            locationClient.stopLocation();
        }
    }

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(true);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(1000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }

    //
    private  String TAG=LocationAlarmService.class.getSimpleName();
    /**
     * 定位监听
     */
    //
            private  AMapLocation lastLoc=null;
            //
    //
    private  long pointIndex=0L;
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {

            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    LocationEvent event=new LocationEvent();
                    // 坐标点相同则不计数
                    if(lastLoc!=null){
                        //
                        if(lastLoc.getLatitude()!=location.getLatitude()&&lastLoc.getLongitude()!=location.getLatitude()){
                            //
                            pointIndex++;
                        }
                    }
                    lastLoc=location;
                    event.setMapLocation(location);
                    event.setPointCount(pointIndex);
                    //
                    EventBus.getDefault().post(event);
                    //

                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                    //
                    Log.d(TAG,sb.toString());
                }

            } else {
            }
        }
    };


//    /**
//     * 获取时间
//     *
//     * @return
//     */
//    public String getTime() {
//        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
//        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
//        String str = formatter.format(curDate);
//        return str;
//    }


    private Notification buildNotification() {

        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = getPackageName()+"001";
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("定位")
                .setContentText("")
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            return builder.getNotification();
        }
        return notification;
    }

    /**
     * 防止后台2个小时后就休眠
     */
    public void startAlarm() {
        AlarmManager am;
        Intent intentAlarm;
        PendingIntent pendSender;
        //首先获得系统服务
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //
        if(am==null)return;
        //设置闹钟的意图，我这里是去调用一个服务，该服务功能就是获取位置并且上传
        intentAlarm = new Intent(this, LocationAlarmService.class);
        pendSender = PendingIntent.getService(this, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
//        am.cancel(pendSender);
        //AlarmManager.RTC_WAKEUP ;这个参数表示系统会唤醒进程；设置的间隔时间是20分钟
//        long triggerAtTime = System.currentTimeMillis() + 20 * 60 * 1000;
        long triggerAtTime = System.currentTimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtTime,pendSender);
//            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtTime,  1000, pendSender);
        }
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            am.setExact(AlarmManager.RTC_WAKEUP, triggerAtTime,
//                    pendSender);
////            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime,  1000, pendSender);
//        }

        else {
            am.set(AlarmManager.RTC_WAKEUP, triggerAtTime,pendSender);
        }
    }


}

