package com.demo.hook;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.demo.hook.util.HookUtil;

import java.io.File;
import java.lang.reflect.Method;

public class MyApplication extends Application {
    private AssetManager assetManager;
    private Resources resources;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            HookUtil.injectPluginClass(this);
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
            String pluginApkPath = new File(getExternalFilesDir(null), "plugin-debug.apk").getAbsolutePath();
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
