package com.aswsl.amap;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.aswsl.amap.rd01.WLWakefulReceiver;
import com.aswsl.amap.services.LocationAlarmService;
import com.aswsl.amap.utils.ActionBarUtil;
import com.aswsl.amap.utils.ConstantUtils;
import com.aswsl.amap.views.CheckPermissionsActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class WLWakeServiceActivity extends CheckPermissionsActivity {

    //
    private MapView mapView = null;
    //
    private AMap aMap = null;
    //
    Intent serviceIntent = null;

    //
    private  String START_LOCATION="startLocation";
    //
    //
    @BindView(R.id.txt_display_info)
    protected TextView infoTextView = null;
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
            points = null;
            stopService(serviceIntent);
            //
            ConstantUtils.XUN_CHA_SERVICE = false;
        } else {
            //
            //
            aMap.clear();
            isClicked = true;
            points = new ArrayList<LatLng>();
            btnLocation.setText("定位中");

            Intent intent=new Intent(START_LOCATION);
            //
            intent.putExtra("msg","start location..");
            //
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        }
    }

    Unbinder unbinder = null;
    //
    private WLWakefulReceiver wlWakefulReceiver=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_location);
        //
        //
        ActionBarUtil.setActionBarTitle(this, R.string.btn_bd_label);

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
        //
        IntentFilter intentFilter=new IntentFilter(START_LOCATION);
        //
        wlWakefulReceiver=new WLWakefulReceiver();
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(wlWakefulReceiver,intentFilter);

    }


    //
    private final String TAG = WLWakeServiceActivity.class.getSimpleName();

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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(wlWakefulReceiver);

        EventBus.getDefault().unregister(this);
    }
}