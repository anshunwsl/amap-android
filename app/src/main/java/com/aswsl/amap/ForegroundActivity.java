package com.aswsl.amap;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.aswsl.amap.utils.ActionBarUtil;
import com.aswsl.amap.utils.NotificationUtils;
import com.aswsl.amap.views.CheckPermissionsActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ForegroundActivity extends CheckPermissionsActivity {

    //
    private MapView mapView = null;
    //
    private AMap aMap = null;
    //
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    //
    Intent serviceIntent = null;
    //

    //
    //
    private boolean isClicked = false;

    @BindView(R.id.bt_location)
    protected Button btnLocation = null;

    //
    @BindView(R.id.txt_display_info)
    protected TextView infoTextView = null;

    //
    @OnClick(R.id.bt_location)
    public void onClick(View v) {
        //
        if (isClicked) {
            isClicked = false;
            btnLocation.setText("停止");
            points = null;
            stopLocation();
        } else {
            //
            //
            aMap.clear();
            isClicked = true;
            points = new ArrayList<LatLng>();
            btnLocation.setText("定位中");
            startLocation();

        }
    }

    Unbinder unbinder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_location);
        mapView = findViewById(R.id.main_map_view);
        //
        //
        ActionBarUtil.setActionBarTitle(this, R.string.btn_foreground_label);
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
        serviceIntent = new Intent();
        serviceIntent.setClass(this, LocationForegoundService.class);

        initView();
        //
        initLocation();
    }
    //
    //

    //初始化控件
    private void initView() {

    }

    boolean isSartLocation = false;
    //
    //

    /**
     * 开始定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void startLocation() {
        //根据控件的选择，重新设置定位参数
        resetOption();
        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
        isSartLocation = true;
    }
    //
    //
    //

    /**
     * 停止定位
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private void stopLocation() {
        // 停止定位
        locationClient.stopLocation();
        isSartLocation = false;
    }
    //

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
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
        isSartLocation = false;
    }

    // 根据控件的选择，重新设置定位参数
    private void resetOption() {
        // 设置是否需要显示地址信息
        locationOption.setNeedAddress(true);
        /**
         * 设置是否优先返回GPS定位结果，如果30秒内GPS没有返回定位结果则进行网络定位
         * 注意：只有在高精度模式下的单次定位有效，其他方式无效
         */
        locationOption.setGpsFirst(true);
        // 设置是否开启缓存
        locationOption.setLocationCacheEnable(true);
        // 设置是否单次定位
        locationOption.setOnceLocation(false);
        //设置是否等待设备wifi刷新，如果设置为true,会自动变为单次定位，持续定位时不要使用
        locationOption.setOnceLocationLatest(false);
        //设置是否使用传感器
        locationOption.setSensorEnable(true);
        //设置是否开启wifi扫描，如果设置为false时同时会停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        String strInterval = "1000";
        if (!TextUtils.isEmpty(strInterval)) {
            try {
                // 设置发送定位请求的时间间隔,最小值为1000，如果小于1000，按照1000算
                locationOption.setInterval(Long.valueOf(strInterval));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        String strTimeout = "5000";
        if (!TextUtils.isEmpty(strTimeout)) {
            try {
                // 设置网络请求超时时间
                locationOption.setHttpTimeOut(Long.valueOf(strTimeout));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


    //
    private static SimpleDateFormat sdf = null;

    public static String formatUTC(long l, String strPattern) {
        if (TextUtils.isEmpty(strPattern)) {
            strPattern = "yyyy-MM-dd HH:mm:ss";
        }
        if (sdf == null) {
            try {
                sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
            } catch (Throwable e) {
            }
        } else {
            sdf.applyPattern(strPattern);
        }
        return sdf == null ? "NULL" : sdf.format(l);
    }
    //

    //
    private final String TAG = ForegroundActivity.class.getSimpleName();

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
    private Marker marker = null;
    //
    private long pointIndex = 0L;
    private AMapLocation lastLoc = null;
    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {

                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if (location.getErrorCode() == 0) {
                    sb.append("定位成功" + "\n");
                    sb.append("定位类型: " + location.getLocationType() + "\n");
                    sb.append("经    度    : " + location.getLongitude() + "\n");
                    sb.append("纬    度    : " + location.getLatitude() + "\n");
                    sb.append("精    度    : " + location.getAccuracy() + "米" + "\n");
                    sb.append("提供者    : " + location.getProvider() + "\n");

                    sb.append("速    度    : " + location.getSpeed() + "米/秒" + "\n");
                    sb.append("角    度    : " + location.getBearing() + "\n");
                    // 获取当前提供定位服务的卫星个数
                    sb.append("星    数    : " + location.getSatellites() + "\n");
                    sb.append("国    家    : " + location.getCountry() + "\n");
                    sb.append("省            : " + location.getProvince() + "\n");
                    sb.append("市            : " + location.getCity() + "\n");
                    sb.append("城市编码 : " + location.getCityCode() + "\n");
                    sb.append("区            : " + location.getDistrict() + "\n");
                    sb.append("区域 码   : " + location.getAdCode() + "\n");
                    sb.append("地    址    : " + location.getAddress() + "\n");
                    sb.append("兴趣点    : " + location.getPoiName() + "\n");
                    //定位完成的时间
                    sb.append("定位时间: " + formatUTC(location.getTime(), "yyyy-MM-dd HH:mm:ss") + "\n");

                    //
                    //
                    if (lastLoc != null) {
                        //
                        if (lastLoc.getLatitude() != location.getLatitude()
                                && lastLoc.getLongitude() != location.getLongitude()) {
                            //
                            pointIndex++;
                        }
                    }
                    //
                    //
                    lastLoc=location;
                    infoTextView.setText("当前采集到位置点个数== " + pointIndex + " 个 ");

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
                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");

//                    Log.d(TAG, sb.toString());
                }
                sb.append("***定位质量报告***").append("\n");
                sb.append("* WIFI开关：").append(location.getLocationQualityReport().isWifiAble() ? "开启" : "关闭").append("\n");
                sb.append("* GPS状态：").append(getGPSStatusString(location.getLocationQualityReport().getGPSStatus())).append("\n");
                sb.append("* GPS星数：").append(location.getLocationQualityReport().getGPSSatellites()).append("\n");
                sb.append("****************").append("\n");
                //定位之后的回调时间
                sb.append("回调时间: " + formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss") + "\n");
                //


                //
//                aMap.setMapStatusLimits();


                //解析定位结果，
//                String result = sb.toString();
//                //
//                Log.d(TAG,result);
//                tvResult.setText(result);
            } else {
//                tvResult.setText("定位失败，loc is null");
                Log.d(TAG, "定位失败，loc is null");
            }
        }
    };
    //

    //

    /**
     * 默认的定位参数
     *
     * @author hongming.wang
     * @since 2.8.0
     */
    private AMapLocationClientOption getDefaultOption() {
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(1000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(false);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    private String getGPSStatusString(int statusCode) {
        String str = "";
        switch (statusCode) {
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
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
    }


    //
    //


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
        //
        locationClient.disableBackgroundLocation(true);
        //如果要一直显示可以不执行
//        if (null != serviceIntent) {
//            stopService(serviceIntent);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        //如果已经开始定位了，显示通知栏
        if (isSartLocation) {
//            if (null != serviceIntent) {
//                startService(serviceIntent);
//            }
            //
            //
            NotificationUtils utils = new NotificationUtils(this.getBaseContext());
            //
            Notification.Builder builder = utils.getAndroidChannelNotification("正在后台定位", "定位进行中");
            Notification notification = builder.build();

            notification.defaults = Notification.DEFAULT_SOUND;
            locationClient.enableBackgroundLocation(2001, notification);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        destroyLocation();
        unbinder.unbind();

        //如果要一直显示可以不执行
//        if (null != serviceIntent) {
//            stopService(serviceIntent);
//        }
        //
        //
        if (null != locationClient) {
            //
            locationClient.disableBackgroundLocation(true);
        }

    }
}
