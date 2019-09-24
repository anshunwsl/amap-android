package com.aswsl.amap.temp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.model.LatLng;
import com.aswsl.amap.R;
import com.aswsl.amap.events.LocationEvent;

import org.greenrobot.eventbus.EventBus;
//import com.amap.api.maps2d.model.LatLng;
//import com.evhai.hyt.R;
//import com.evhai.hyt.service.LocationCallBack;
//import com.evhai.hyt.utils.LogUtil;
//import com.evhai.hyt.utils.http.MyCallBack;

/**
 *
 */
public class MLocation {
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //android 8.0后台定位权限
    private static final String NOTIFICATION_CHANNEL_NAME = "Location";
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    public double lat;
    public double lon;
    public LatLng latLng;
    public String address;
    public String city;

    private volatile static MLocation instance;


//    public MyCallBack myCallBack;
//    public LocationCallBack locationCallBack;

    private NotificationManager notificationManager = null;
    private boolean isCreateChannel = false;

    public static MLocation getInstance() {
        synchronized (MLocation.class) {
            if (instance == null) {
                instance = new MLocation();
            }
        }
        return instance;
    }

    public void initOption(Context context) {

        mLocationClient = new AMapLocationClient(context.getApplicationContext());
//设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        //初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
//设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //获取一次定位结果：
        //该方法默认为false。
        mLocationOption.setOnceLocation(false);
        //获取最近3s内精度最高的一次定位结果：
        //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次
        // 定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
        mLocationOption.setOnceLocationLatest(false);
        //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。
        mLocationOption.setInterval(3000);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否允许模拟位置,默认为true，允许模拟位置
        mLocationOption.setMockEnable(true);
        //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
        mLocationOption.setHttpTimeOut(30000);
        //关闭缓存机制
        mLocationOption.setLocationCacheEnable(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.enableBackgroundLocation(2004, buildNotification(context));
        //启动定位
        mLocationClient.startLocation();
    }

    private Notification buildNotification(Context context) {
        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
            if (null == notificationManager) {
                notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            }
            String channelId = context.getPackageName()+"001";
            if (!isCreateChannel) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId,
                        NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                notificationManager.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(context.getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(context.getApplicationContext());
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


    private  long pointIndex=0L;

    private  AMapLocation lastLoc=null;
    //声明定位回调监听器
    AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation location) {
            if (location != null) {
                if (location.getErrorCode() == 0) {
                    //可在其中解析amapLocation获取相应内容。
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
//                    if (myCallBack != null) {
//                        myCallBack.success(aMapLocation.getCity());
//                        myCallBack = null;
//                    }
//                    if (locationCallBack!=null){
//                        locationCallBack.success();
//                    }
                } else {
                    //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + location.getErrorCode() + ", errInfo:"
                            + location.getErrorInfo());
                }
            }
        }
    };
}
