package com.demo.hook;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.demo.hook.util.HookUtil;
import com.demo.hook.util.Reflection;

import java.io.File;
import java.lang.reflect.Method;

public class MyApplication extends Application {
    private AssetManager assetManager;
    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Reflection.unseal(this)) {// 为了在Android P及之后能拿到@hide的方法，如：getService()
            Log.e("gxd", "开启访问@hide失败!");
        }
        try {
            HookUtil.mergeDex(this);
            HookUtil.hookActivityManager(this);
            HookUtil.hookActivityThreadHandlerCallback(this);
        } catch (Exception e) {
            Log.e("gxd", "MyApplication.onCreate-->", e);
        }

        try {
            Class<AssetManager> AssetManagerClass = AssetManager.class;
            assetManager = AssetManagerClass.newInstance();
            Method addAssetPathMethod = AssetManagerClass.getDeclaredMethod("addAssetPath", String.class);
            addAssetPathMethod.setAccessible(true);
            String pluginApkPath = new File(getExternalFilesDir(null), HookUtil.PLUGIN_FILE_NAME).getAbsolutePath();
            addAssetPathMethod.invoke(assetManager, pluginApkPath);
        } catch (Exception e) {
            Log.e("gxd", "Reflect AssetManager-->", e);
        }

        Resources supResource = getResources();
        resources = new Resources(assetManager, supResource.getDisplayMetrics(), supResource.getConfiguration());
    }

    @Override
    public Resources getResources() {
        return resources == null ? super.getResources() : resources;
    }

    @Override
    public AssetManager getAssets() {
        return assetManager == null ? super.getAssets() : assetManager;
    }
}
