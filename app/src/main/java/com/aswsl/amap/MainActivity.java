package com.aswsl.amap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity {

    //
    private Unbinder unbinder=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
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
        Intent intent=new Intent(this,ForegroundActivity.class);
        //
        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
