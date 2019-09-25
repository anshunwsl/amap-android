package com.aswsl.amap.rd03;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.aswsl.amap.AMapApplication;
import com.aswsl.amap.DaemServiceActivity;
import com.aswsl.amap.events.LocationEvent;
import com.shihoo.daemon.work.AbsWorkService;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


/**
 * Created by shihoo ON 2018/12/13.
 * Email shihu.wang@bodyplus.cc 451082005@qq.com
 */
public class MainWorkService extends AbsWorkService implements  AMapLocationListener{

    private Disposable mDisposable;
    private long mSaveDataStamp;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    //
    //
    private  boolean isInit=true;


    private void createLocationClient() {
        //
        if(isInit){
            //
            isInit=false;
            mLocationClient = new AMapLocationClient(AMapApplication.context);
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(1000);
            //设置是否只定位一次,默认为false
//            mLocationOption.setOnceLocation(true);
            //返回最近3s内精度最高的一次定位结果。
            mLocationOption.setOnceLocationLatest(false);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
            //关闭缓存机制
            mLocationOption.setLocationCacheEnable(false);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            mLocationClient.setLocationListener(this);
        }
    }
    //


    @Override
    public void onCreate() {
        //
        Notification notification=buildNotify(getApplicationContext());
//        startForeground(1001,notification);
        super.onCreate();
        //
        this.createLocationClient();
    }
    //
    private static final String CHANNEL_ID = "保活图腾";
    private static final int CHANNEL_POSITION = 1;
    private int value;
    //

    private Notification buildNotify(Context service){
        NotificationManager manager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);

        //
        Notification notification=null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"主服务",
                    NotificationManager.IMPORTANCE_DEFAULT);
            //是否绕过请勿打扰模式
            channel.canBypassDnd();
            //闪光灯
            channel.enableLights(true);
            //锁屏显示通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            //闪关灯的灯光颜色
            channel.setLightColor(Color.RED);
            //桌面launcher的消息角标
            channel.canShowBadge();
            //是否允许震动
            channel.enableVibration(true);
            //获取系统通知响铃声音的配置
            channel.getAudioAttributes();
            //获取通知取到组
            channel.getGroup();
            //设置可绕过  请勿打扰模式
            channel.setBypassDnd(true);
            //设置震动模式
            channel.setVibrationPattern(new long[]{100, 100, 200});
            //是否会有灯光
            channel.shouldShowLights();
            manager.createNotificationChannel(channel);
            notification = new Notification.Builder(service,CHANNEL_ID)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }else {
            notification = new Notification.Builder(service)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }

        //

        return notification;


    }

    /**
     * 是否 任务完成, 不再需要服务运行?
     * @return 应当停止服务, true; 应当启动服务, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean needStartWorkService() {
        return DaemServiceActivity.isCanStartWorkService;
    }

    /**
     * 任务是否正在运行?
     * @return 任务正在运行, true; 任务当前不在运行, false; 无法判断, 什么也不做, null.
     */
    @Override
    public Boolean isWorkRunning() {
        //若还没有取消订阅, 就说明任务仍在运行.
        return mDisposable != null && !mDisposable.isDisposed();
    }

    @Override
    public IBinder onBindService(Intent intent, Void v) {
        // 此处必须有返回，否则绑定无回调
        return new Messenger(new Handler()).getBinder();
    }

    @Override
    public void onServiceKilled() {
        saveData();
        Log.d("wsh-daemon", "onServiceKilled --- 保存数据到磁盘");
    }

    @Override
    public void stopWork() {
        //取消对任务的订阅
        if (mDisposable !=null && !mDisposable.isDisposed()){
            mDisposable.dispose();
        }
        saveData();
    }

    //
    private  String TAG=MainWorkService.class.getSimpleName();
    //
    /**
     * 定位监听
     */
    //
    private  AMapLocation lastLoc=null;
    //
    //
    private  long pointIndex=0L;

    //


    @Override
    public void onLocationChanged(AMapLocation location) {
        //
        String info="------------------------\n"+
                "经度="+location.getLongitude()+"\n"+
                "纬度="+location.getLatitude()+"\n"+
                "地址="+location.getAddress()+"\n"+
                "------------------------";
        Log.d(TAG,info);

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
    }
    //
    //
    private  void  startLocation(){
        if(mLocationClient.isStarted()){
            //
            mLocationClient.stopLocation();
        }
        mLocationClient.startLocation();
    }

    @Override
    public void startWork() {
        Log.d("wsh-daemon", "检查磁盘中是否有上次销毁时保存的数据");
        //
        //
        //
        startLocation();
        mDisposable = Observable
                .interval(30, TimeUnit.MINUTES)
                //取消任务时取消定时唤醒
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d("wsh-daemon", " -- doOnDispose ---  取消订阅 .... ");
                        saveData();
                    }
                })
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {

                        Log.d("wsh-daemon", "每 3 秒采集一次数据... count = " + aLong);
                        //

                        //
                        startLocation();

                        if (aLong > 0 && aLong % 18 == 0){
                            saveData();
                            Log.d("wsh-daemon", "   采集数据  saveCount = " + (aLong / 18 - 1));
                        }
                    }
                });
    }


    private void saveData(){
        long stamp = System.currentTimeMillis()/1000;
        if (Math.abs(mSaveDataStamp - stamp) >= 3){
            // 处理业务逻辑
            Log.d("wsh-daemon", "保存数据到磁盘。");
        }
        mSaveDataStamp = stamp;
    }

}
