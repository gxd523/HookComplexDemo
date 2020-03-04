package com.demo.hook.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.demo.hook.R;
import com.demo.hook.util.InterceptorUtil;

public class MainActivity extends Activity {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int drawableResId = getApplication().getResources().getIdentifier("ic_plugin", "drawable", "com.demo.plugin");
        try {
            Drawable drawable = getDrawable(drawableResId);
            if (drawable != null) {
                Log.d("gxd", "拿到插件中的资源文件");
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
