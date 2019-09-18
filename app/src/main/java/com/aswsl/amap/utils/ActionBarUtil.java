package com.aswsl.amap.utils;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by agisv2 on 2019-08-03 14:30
 *
 * @author agisv2 wangsl@dse.cn
 * @version 1.0.0
 */
public class ActionBarUtil {
    //
    public  static  void  setActionBarTitle(AppCompatActivity activity,int title){
        //
        if(activity.getActionBar()!=null){
            activity.getActionBar().setTitle(title);
            return;
        }
        //
        if(activity.getSupportActionBar()!=null){
            activity.getSupportActionBar().setTitle(title);
        }
    }
}
