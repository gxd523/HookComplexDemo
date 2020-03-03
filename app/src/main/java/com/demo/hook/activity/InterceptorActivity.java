package com.demo.hook.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.demo.hook.R;

public class InterceptorActivity extends Activity {
    public static final String EXTRA_INTENT = "extraIntent";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interceptor);
        String className = getIntent().getStringExtra(EXTRA_INTENT);
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText("拦截..." + className);
        }
    }
}
