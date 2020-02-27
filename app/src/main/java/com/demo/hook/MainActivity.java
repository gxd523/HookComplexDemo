package com.demo.hook;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpSecondActivity(View view) {
//        系统里面做了手脚   --》newIntent   msg--->obj-->intent
        startActivity(new Intent(this, SecondActivity.class));
    }

    public void jumpThirdActivity(View view) {
        startActivity(new Intent(this, ThirdActivity.class));
    }

    public void jumpForthActivity(View view) {
        startActivity(new Intent(this, ForthActivity.class));
    }

    public void logout(View view) {
        SharedPreferences share = this.getSharedPreferences("alan", MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putBoolean("login", false);
        editor.apply();
        Toast.makeText(this, "退出登录成功", Toast.LENGTH_SHORT).show();
    }
}
