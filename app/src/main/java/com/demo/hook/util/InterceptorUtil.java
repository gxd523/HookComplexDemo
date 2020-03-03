package com.demo.hook.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by guoxiaodong on 2020/2/28 16:08
 */
public class InterceptorUtil {
    public static final InterceptorUtil instance = new InterceptorUtil();
    private static final String IS_INTERCEPT = "IS_INTERCEPT";
    private SharedPreferences sp;

    private InterceptorUtil() {
    }

    public void init(Context context) {
        sp = context.getSharedPreferences("gxd", Context.MODE_PRIVATE);
    }

    public boolean isIntercept() {
        return sp.getBoolean(IS_INTERCEPT, true);
    }

    public boolean setInterceptor() {
        SharedPreferences.Editor editor = sp.edit();
        boolean newValue = !isIntercept();
        editor.putBoolean(IS_INTERCEPT, newValue);
        editor.apply();
        return newValue;
    }
}
