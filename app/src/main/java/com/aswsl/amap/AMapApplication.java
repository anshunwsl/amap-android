package com.aswsl.amap;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by agisv2 on 2019-08-01 17:18
 *
 * @author agisv2 wangsl@dse.cn
 * @version 1.0.0
 */
public class AMapApplication extends Application {
    //

    private RefWatcher refWatcher;
    @Override
    public void onCreate() {
        super.onCreate();
        //
        //        LeakCanary.install(this);
        refWatcher=setupLeakCanary();

    }
    //
    //
    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        AMapApplication leakApplication = (AMapApplication) context.getApplicationContext();
        return leakApplication.refWatcher;
    }
}
