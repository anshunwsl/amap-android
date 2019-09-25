package com.aswsl.amap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.aswsl.amap.rd01.WLWakefulReceiver;
import com.aswsl.amap.rd02.Demo;
import com.aswsl.amap.rd03.MainDamActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    //
    private Unbinder unbinder=null;
    //
    private WLWakefulReceiver wlWakefulReceiver=null;
    //
    private  String START_LOCATION="startLocation";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        //
        //
        wlWakefulReceiver=new WLWakefulReceiver();
        //
        IntentFilter intentFilter=new IntentFilter(START_LOCATION);
        //
//        registerReceiver( wlWakefulReceiver,intentFilter);
        //
        LocalBroadcastManager.getInstance(this).registerReceiver(wlWakefulReceiver,intentFilter);
      unbinder=  ButterKnife.bind(this);
    }
    //
    //
    @OnClick(R.id.btn_alarm)
    public void   onBtnAlarmClick(View view){
        //
        //
        Intent intent=new Intent(this,AlarmMainActivity.class);
        //
        startActivity(intent);
    }

    @OnClick(R.id.btn_alarm_service)
    public void   onBtnAlarmServiceClick(View view){
        //
        //
        Intent intent=new Intent(this,AlarmServiceActivity.class);
        //
        startActivity(intent);
    }
    //
    //

    @OnClick(R.id.btn_foreground)
    public  void onBtnForegroundClick(View view){
        //
//        Intent intent=new Intent(this,ForegroundActivity.class);
        Intent intent=new Intent(this,DaemServiceActivity.class);
        //
        startActivity(intent);

    }

    //


    @OnClick(R.id.btn_test_service)
    public void  onBtnTestClick(View view){
        //
        //
//        Intent intent=new Intent(this,TestActivity.class);
//        startActivity(intent);
        //
        Intent intent=new Intent(this, DaemServiceActivity.class);
        //
        startActivity(intent);
    }
    //
    //
    @OnClick(R.id.btn_bd)
    public  void  onBtnBdClick(View view){
        //
//        Intent intent = new Intent();
//        //
//        intent.setAction(START_LOCATION);
//        intent.putExtra("msg", "定位定位定位");
//        //
//        Log.d("dd","start location...");
//        //
//
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        sendBroadcast(intent);
        //

        //Inte
        Intent intent=new Intent(this,WLWakeServiceActivity.class);
        //
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        //
        if(wlWakefulReceiver!=null){
            //
            unregisterReceiver(wlWakefulReceiver);
        }
        //

    }
}
