package com.demo.hook.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.demo.hook.R;
import com.demo.hook.util.InterceptorUtil;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpTargetActivity(View view) {
        startActivity(new Intent(this, TargetActivity.class));
    }

    public void setInterceptor(View view) {
        ((TextView) view).setText(InterceptorUtil.instance.setInterceptor() ? "拦截器已开启" : "拦截器已关闭");
    }
}
