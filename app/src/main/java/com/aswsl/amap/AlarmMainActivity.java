package com.aswsl.amap;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.aswsl.amap.services.LocationForegoundService;
import com.aswsl.amap.utils.NotificationUtils;
import com.aswsl.amap.utils.Utils;
import com.aswsl.amap.views.CheckPermissionsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AlarmMainActivity extends CheckPermissionsActivity implements
        View.OnClickListener, AMapLocationListener  {

    //
    private MapView mapView = null;
    //
    private AMap aMap = null;
    //
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;

    private Intent alarmIntent = null;
    private PendingIntent alarmPi = null;
    private AlarmManager alarm = null;
    //

    private  int gpsRefreshInterval=1000;
    //

    //
    private UiSettings uiSettings=null;

    //
    //
    private boolean isClicked = false;

    @BindView(R.id.bt_location)
    protected Button btnLocation = null;

    //
    @OnClick(R.id.bt_location)
    public void onClick(View v) {
        //
        if (isClicked) {
            isClicked = false;
            btnLocation.setText("停止");
            points=null;
            //

            // 停止定位
            isStartLocation=false;
            locationClient.stopLocation();
            mHandler.sendEmptyMessage(Utils.MSG_LOCATION_STOP);

            //停止定位的时候取消闹钟
            if(null != alarm){
                alarm.cancel(alarmPi);
            }


        } else {
            //
            aMap.clear();
            isClicked = true;
            points=new ArrayList<LatLng>();
            btnLocation.setText("定位中");
            initOption();
            int alarmInterval = 5;
            // 设置定位参数
            locationClient.setLocationOption(locationOption);
            // 启动定位
            locationClient.startLocation();
            isStartLocation=true;
            mHandler.sendEmptyMessage(Utils.MSG_LOCATION_START);

            if(null != alarm){
                //设置一个闹钟，2秒之后每隔一段时间执行启动一次定位程序
                alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 2*1000,
                        gpsRefreshInterval*5, alarmPi);
            }

        }
    }
    //

    // 根据控件的选择，重新设置定位参数
    private void initOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        locationOption.setInterval(gpsRefreshInterval);

    }

    Unbinder unbinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.main_map_view);
        //
        mapView.onCreate(savedInstanceState);
        //
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.showMapText(true);

        //
        uiSettings=aMap.getUiSettings();
        //
        uiSettings.setZoomControlsEnabled(false);
        //
        unbinder = ButterKnife.bind(this);
        //
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = new AMapLocationClientOption();
        // 设置定位模式为高精度模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位监听
        locationClient.setLocationListener(this);
        // 创建Intent对象，action为LOCATION
        alarmIntent = new Intent();
        alarmIntent.setAction("LOCATION");
        IntentFilter ift = new IntentFilter();

        // 定义一个PendingIntent对象，PendingIntent.getBroadcast包含了sendBroadcast的动作。
        // 也就是发送了action 为"LOCATION"的intent
        alarmPi = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        // AlarmManager对象,注意这里并不是new一个对象，Alarmmanager为系统级服务
        alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        //动态注册一个广播
        IntentFilter filter = new IntentFilter();
        filter.addAction("LOCATION");
        registerReceiver(alarmReceiver, filter);
    }
    @Override
    public void onLocationChanged(AMapLocation loc) {

        if (null != loc) {
            Message msg = mHandler.obtainMessage();
            msg.obj = loc;
            msg.what = Utils.MSG_LOCATION_FINISH;
            mHandler.sendMessage(msg);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    boolean isStartLocation = false;
    //
    //
    private BroadcastReceiver alarmReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("LOCATION")){
                if(null != locationClient){
                    locationClient.startLocation();
                    isStartLocation=true;
                }
            }
        }
    };
    //

    Handler mHandler = new Handler() {
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                //开始定位
                case Utils.MSG_LOCATION_START:
                    btnLocation.setText("定位中");
                    break;
                // 定位完成
                case Utils.MSG_LOCATION_FINISH:
                    AMapLocation location = (AMapLocation) msg.obj;
                    StringBuilder builder = new StringBuilder();
                    //
                    builder.append("-------------------");
                    builder.append("经度：" + location.getLongitude());
                    builder.append("纬度：" + location.getLatitude());

                    builder.append("提供者    : " + location.getProvider() + "\n");
                    //
                    //
                    builder.append("--------------");
//                //
                    Log.d(TAG, builder.toString());
                    //

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    //
                    //
                    if (marker!=null){
                        //
                        marker.remove();
                    }
                    marker = aMap.addMarker(new MarkerOptions().position(latLng));
                    //
                    points.add(latLng);
                    //
                    //
                    drawPolyline(points);
                    //设置中心点和缩放比例
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
                    break;
                //停止定位
                case Utils.MSG_LOCATION_STOP:
                    btnLocation.setText("停止");
                    break;
                default:
                    break;
            }
        };
    };
    //
    private final String TAG = AlarmMainActivity.class.getSimpleName();


    private Polyline polyline=null;
    //
    private  List<LatLng> points=new ArrayList<LatLng>();
    private  void  drawPolyline( List<LatLng> latLngs){
        //
        if(polyline!=null){
            //
            polyline.remove();
        }
        polyline =aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.argb(125, 255, 0, 0)));

    }
    //
    private Marker marker=null;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        locationClient.disableBackgroundLocation(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //如果已经开始定位了，显示通知栏
        if (isStartLocation) {
            NotificationUtils utils=new NotificationUtils(this.getBaseContext());
            //
            Notification.Builder builder=utils.getAndroidChannelNotification("正在后台定位","定位进行中");
            Notification notification=builder.build();

            notification.defaults = Notification.DEFAULT_SOUND;
            locationClient.enableBackgroundLocation(2001,notification);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unbinder.unbind();
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }

        if(null != alarmReceiver){
            unregisterReceiver(alarmReceiver);
            alarmReceiver = null;
        }
    }
}
