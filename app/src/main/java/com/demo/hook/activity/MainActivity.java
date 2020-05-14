package com.demo.hook.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.demo.hook.R;
import com.demo.hook.util.InterceptorUtil;

/**
 * 1、加载plugin的dex到主应用dex的classLoader上
 * 2、替换IActivityManager实例为我们的代理ActivityManager，修改其startActivity()，intent替换为注册的proxyActivity
 * 3、将ActivityThread里的Handler对象的callback对象替换为我们的callback
 * 4、从Callback的handleMessage()中msg.obj获取intent，将跳转修改为targetActivity(注意返回false，否则就不会走handler的handleMessage())
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int drawableResId = getApplication().getResources().getIdentifier("ic_plugin", "drawable", "com.demo.plugin");
        try {
            Drawable drawable = getApplication().getResources().getDrawable(drawableResId);
            if (drawable != null) {
                Log.e("gxd", "拿到插件中的资源文件!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void jumpTargetActivity(View view) {
        try {
            Class TargetActivityClass = Class.forName("com.demo.plugin.TargetActivity");
            startActivity(new Intent(this, TargetActivityClass));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setInterceptor(View view) {
        ((TextView) view).setText(InterceptorUtil.instance.setInterceptor() ? "拦截器已开启" : "拦截器已关闭");
    }
}
