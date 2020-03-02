package com.demo.hook;

import android.app.Application;
import android.util.Log;

import com.demo.hook.util.HookUtil;
import com.demo.hook.util.LoginUtil;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            LoginUtil.instance.init(this);
            HookUtil.hookActivityManager(this);
            HookUtil.hookActivityThreadHandlerCallback(this);
        } catch (Exception e) {
            Log.d("gxd", "MyApplication.onCreate-->", e);
        }
    }
}
