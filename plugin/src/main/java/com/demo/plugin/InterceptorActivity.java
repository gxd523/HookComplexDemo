package com.demo.plugin;

import android.os.Bundle;
import android.widget.TextView;

public class InterceptorActivity extends BasePluginActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interceptor);
        String className = getIntent().getStringExtra("extraIntent");
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText("拦截..." + className);
        }
    }
}
