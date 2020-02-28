package com.demo.hook;

import android.app.Application;

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
            e.printStackTrace();
        }
    }
}
