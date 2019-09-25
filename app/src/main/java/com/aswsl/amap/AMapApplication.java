package com.aswsl.amap;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.aswsl.amap.rd03.ApkHelper;
import com.aswsl.amap.rd03.MainWorkService;
import com.shihoo.daemon.ForegroundNotificationUtils;
import com.shihoo.daemon.watch.WatchProcessPrefHelper;
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
    //
    public  static  Context context=null;
    @Override
    public void onCreate() {
        super.onCreate();
        //
        //        LeakCanary.install(this);
        refWatcher=setupLeakCanary();
        //
        context=this;
        //

        //需要在 Application 的 onCreate() 中调用一次 DaemonEnv.initialize()
        // 每一次创建进程的时候都需要对Daemon环境进行初始化，所以这里没有判断进程


        String processName = ApkHelper.getProcessName(this.getApplicationContext());
        if ("com.dse.app".equals(processName)){
            // 主进程 进行一些其他的操作
            Log.d("wsh-daemon", "启动主进程");

        }else if ("com.shihoo.daemonlibrary:work".equals(processName)){
            Log.d("wsh-daemon", "启动了工作进程");
        }else if ("com.shihoo.daemonlibrary:watch".equals(processName)){
            // 这里要设置下看护进程所启动的主进程信息
            WatchProcessPrefHelper.mWorkServiceClass = MainWorkService.class;
            // 设置通知栏的UI
            ForegroundNotificationUtils.setResId(R.drawable.ic_launcher_background);
            ForegroundNotificationUtils.setNotifyTitle("我是");
            ForegroundNotificationUtils.setNotifyContent("渣渣辉");
            Log.d("wsh-daemon", "启动了看门狗进程");
        }

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
