package com.demo.hook;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ProxyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
        Log.d("gxd", "ProxyActivity.onCreate...");
    }
}
