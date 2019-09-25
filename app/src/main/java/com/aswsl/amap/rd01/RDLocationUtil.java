package com.aswsl.amap.rd01;

import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.aswsl.amap.AMapApplication;

public class RDLocationUtil implements AMapLocationListener {
    //

    //

    private static RDLocationUtil locationUtil;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private OnLocationBack onLocationBack;
    private OnLocationTrain onLocationTrain;
    public static final String NULL = "null";
    private String differenceFlag = "";
    private String latitude, longitude, cityNameString, HotelCityCode;
    //
    //
    private  boolean isInit=true;


    private RDLocationUtil() {

    }

    public static RDLocationUtil getInstance() {
        if (locationUtil == null) {
            synchronized (RDLocationUtil.class) {
                if (locationUtil == null) {
                    locationUtil = new RDLocationUtil();
                }
            }
        }
        return locationUtil;
    }

    private void init() {
        //
        if(isInit){
            //
            isInit=false;
            mLocationClient = new AMapLocationClient(AMapApplication.context);
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位间隔,单位毫秒,默认为2000ms
//        mLocationOption.setInterval(2000);
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

    public void startLocation(String differenceFlag, OnLocationBack onLocationBack) {
        init();
        //
        if(mLocationClient.isStarted()){
            //
            mLocationClient.stopLocation();
        }
        mLocationClient.startLocation();//开始
        this.onLocationBack = onLocationBack;
        this.differenceFlag = differenceFlag;
        Log.e("开始定位","开始定位");
    }

    public void startLocationTrain(String differenceFlag, OnLocationTrain onLocationTrain) {
        init();
        mLocationClient.startLocation();//开始
        this.onLocationTrain = onLocationTrain;
        this.differenceFlag = differenceFlag;
        Log.e("开始定位","开始定位");
    }

    public void stopLocation() {
        if (null == mLocationClient) {
            return;
        }
        mLocationClient.unRegisterLocationListener(this);
        mLocationClient.stopLocation();//关闭
        mLocationClient.onDestroy();//销毁
        mLocationClient = null;
        Log.e("开始定位","开始定位");
    }


    @Override
    public void onLocationChanged(AMapLocation location) {
        Log.e("定位到当前位置:  " , location.getAddress());
        //

        if (null != location.getCity()
                && !"null".equals(location.getCity())
                && !"".equals(location.getCity())
                && 0 != location.getLatitude()) {
            cityNameString = location.getCity();
            latitude = "" + location.getLatitude();
            longitude = "" + location.getLongitude();
            saveLocation(location);
        } else {
            onLocationTrain.LocationFail("定位失败");
            return;
        }

    }

    public interface OnLocationBack {
        void back(AMapLocation aMapLocation, String backString);
    }

    public interface OnLocationTrain {
        void back(AMapLocation aMapLocation, String backString);
        void LocationFail(String error);
    }

    private void saveLocation(AMapLocation aMapLocation) {
        switch (differenceFlag) {
            case NULL:
                onLocationBack.back(aMapLocation, "返回的是定位到的所有信息");
                break;
        }
    }
}