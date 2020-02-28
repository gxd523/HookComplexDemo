package com.demo.hook.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.demo.hook.util.LoginUtil;
import com.demo.hook.R;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpSecondActivity(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void jumpThirdActivity(View view) {
        startActivity(new Intent(this, ThirdActivity.class));
    }

    public void jumpForthActivity(View view) {
        startActivity(new Intent(this, ForthActivity.class));
    }

    public void logout(View view) {
        LoginUtil.instance.logout();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}
