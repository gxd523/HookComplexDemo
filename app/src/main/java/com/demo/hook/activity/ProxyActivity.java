package com.demo.hook.activity;

import android.app.Activity;

/**
 * 实际并不会启动ProxyActivity,只是因为ProxyActivity在清单注册过,为了骗过ActivityManager
 */
public class ProxyActivity extends Activity {
}
