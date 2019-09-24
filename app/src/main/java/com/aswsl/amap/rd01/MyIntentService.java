package com.aswsl.amap.rd01;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.aswsl.amap.events.LocationEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

public class MyIntentService extends IntentService {
    public MyIntentService() {
        super("MyIntentService");
        //
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //

    //
    private  final  String TAG=MyIntentService.class.getSimpleName();
    //
    /**
     * 定位监听
     */
    //
    private  AMapLocation lastLoc=null;
    //
    //
    private  long pointIndex=0L;
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        //子线程中执行
        Log.i("MyIntentService", "onHandleIntent");
        String extra = intent.getStringExtra("msg");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //
                RDLocationUtil.getInstance().startLocation(RDLocationUtil.NULL, new RDLocationUtil.OnLocationBack() {
                    @Override
                    public void back(AMapLocation location, String backString) {

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
                });
                //

            }
        }).start();

        Log.i("MyIntentService", "onHandleIntent:"+extra);
        //调用completeWakefulIntent来释放唤醒锁。
//       WLWakefulReceiver.completeWakefulIntent(intent);
    }
}