package com.demo.hook.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by guoxiaodong on 2020/2/28 16:08
 */
public class LoginUtil {
    public static final LoginUtil instance = new LoginUtil();
    private static final String IS_NEED_LOGIN = "IS_NEED_LOGIN";
    private SharedPreferences sp;


    private LoginUtil() {
    }

    public void init(Context context) {
        sp = context.getSharedPreferences("gxd", Context.MODE_PRIVATE);
    }

    public boolean isNeedLogin() {
        return sp.getBoolean(IS_NEED_LOGIN, true);
    }

    public void logout() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(IS_NEED_LOGIN, true);
        editor.apply();
    }

    public void login() {
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(IS_NEED_LOGIN, false);
        editor.apply();
    }
}
