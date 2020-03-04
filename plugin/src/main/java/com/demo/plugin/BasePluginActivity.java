package com.demo.plugin;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by guoxiaodong on 2020/3/4 10:23
 */
public abstract class BasePluginActivity extends Activity {
    @Override
    public Resources getResources() {
        if (getApplication() != null && getApplication().getResources() != null) {
            return getApplication().getResources();
        }
        Log.d("gxd", getClass().getSimpleName() + ".getResources-->null");
        return super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (getApplication() != null && getApplication().getAssets() != null) {
            return getApplication().getAssets();
        }
        Log.d("gxd", getClass().getSimpleName() + ".getAssets-->null");
        return super.getAssets();
    }
}
