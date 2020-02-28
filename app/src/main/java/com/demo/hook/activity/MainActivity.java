package com.demo.hook.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.demo.hook.R;
import com.demo.hook.util.LoginUtil;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpTargetActivity(View view) {
        startActivity(new Intent(this, TargetActivity.class));
    }

    public void logout(View view) {
        LoginUtil.instance.logout();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}
