package com.aswsl.amap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.aswsl.amap.events.LocationEvent;
import com.aswsl.amap.rd03.MainWorkService;
import com.aswsl.amap.views.CheckPermissionsActivity;
import com.shihoo.daemon.DaemonEnv;
import com.shihoo.daemon.IntentWrapper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DaemServiceActivity extends CheckPermissionsActivity {

    //
    //是否 任务完成, 不再需要服务运行? 最好使用SharePreference，注意要在同一进程中访问该属性
    public static boolean isCanStartWorkService;
    //

    private MapView mapView = null;
    //
    private AMap aMap = null;
    //
    //
    @BindView(R.id.txt_display_info)
    protected TextView infoTextView = null;
    //
    private boolean isClicked = false;

    @BindView(R.id.bt_location)
    protected Button btnLocation = null;

    private int value;
    //
    @OnClick(R.id.bt_location)
    public void onClick(View v) {
        //
        if (isClicked) {
            isClicked = false;
            btnLocation.setText("停止");
            points = null;

            //
            value ++;
//                buildNotify(this);
            DaemonEnv.sendStopWorkBroadcast(this);
            isCanStartWorkService = false;
        } else {
            //
            //
            aMap.clear();
            isClicked = true;
            points = new ArrayList<LatLng>();
            btnLocation.setText("定位中");

            DaemonEnv.sendStartWorkBroadcast(this);
            //
//            isCanStartWorkService = true;
///
//            DaemonEnv.startServiceSafely(DaemServiceActivity.this, MainWorkService.class);
            v.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCanStartWorkService = true;

                    DaemonEnv.startServiceSafely(DaemServiceActivity.this, MainWorkService.class);
                }
            },1000);

//            buildNotify(this);

        }
    }
    //


    private static final String CHANNEL_ID = "保活图腾";
    private static final int CHANNEL_POSITION = 1;


    private void buildNotify(Context service){
        NotificationManager manager = (NotificationManager)service.getSystemService(Context.NOTIFICATION_SERVICE);
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
            Notification notification = new Notification.Builder(service,CHANNEL_ID)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }else {
            Notification notification = new Notification.Builder(service)
                    .setContentTitle("我是通知哦哦")//设置标题
                    .setContentText("我是通知内容..."+value)//设置内容
                    .setWhen(System.currentTimeMillis())//设置创建时间
                    .setSmallIcon(com.shihoo.daemon.R.drawable.icon1)//设置状态栏图标
                    .setLargeIcon(BitmapFactory.decodeResource(service.getResources(), com.shihoo.daemon.R.drawable.icon1))//设置通知栏图标
                    .build();
            manager.notify(CHANNEL_POSITION,notification);
        }
    }

    //

    //防止华为机型未加入白名单时按返回键回到桌面再锁屏后几秒钟进程被杀
    public void onBackPressed() {
        IntentWrapper.onBackPressed(this);
    }
    //

    Unbinder unbinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_location);
        //
        //
//        ActionBarUtil.setActionBarTitle(this, R.string.btn_test_service_label);

        mapView = findViewById(R.id.main_map_view);
        //
        mapView.onCreate(savedInstanceState);
        //
        aMap = mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);
        aMap.showMapText(true);

        //
        //
        UiSettings uiSettings = aMap.getUiSettings();
        //
        uiSettings.setZoomControlsEnabled(false);
        //
        unbinder = ButterKnife.bind(this);
        //
        if (!EventBus.getDefault().isRegistered(this)) {
            //
            EventBus.getDefault().register(this);
        }
        //

    }


    //
    private final String TAG = DaemServiceActivity.class.getSimpleName();

    //

    private Polyline polyline = null;
    //
    private List<LatLng> points = new ArrayList<LatLng>();

    private void drawPolyline(List<LatLng> latLngs) {
        //
        if (polyline != null) {
            //
            polyline.remove();
        }
        polyline = aMap.addPolyline(new PolylineOptions().
                addAll(latLngs).width(10).color(Color.argb(125, 255, 0, 0)));

    }

    //

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onLocationChanged(LocationEvent event) {
        //
        //
        //
        //
        infoTextView.setText("当前采集到位置点个数== " + event.getPointCount() + " 个 ");
        AMapLocation location = event.getMapLocation();

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

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //
        //
        if (marker != null) {
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
//                    Log.d(TAG, sb.toString());

    }

    //
    private Marker marker = null;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        unbinder.unbind();
        //
        EventBus.getDefault().unregister(this);
    }
}
