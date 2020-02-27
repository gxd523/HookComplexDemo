package com.demo.hook;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
    EditText nameEt;
    EditText pwdEt;
    SharedPreferences sp;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEt = findViewById(R.id.name_et);
        pwdEt = findViewById(R.id.pwd_et);
        sp = this.getSharedPreferences("alan", MODE_PRIVATE);
        className = getIntent().getStringExtra("extraIntent");
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText("来自界面：" + className);
        }
    }

    public void login(View view) {
        if ((nameEt.getText() == null || pwdEt.getText() == null)) {
            Toast.makeText(this, "请填写用户名 或密码", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        if ("abc".equals(nameEt.getText().toString()) && "123".equals(pwdEt.getText().toString())) {
            editor.putString("name", nameEt.getText().toString());
            editor.putString("pwd", pwdEt.getText().toString());
            editor.putBoolean("login", true);
            editor.apply();
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            if (className != null) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(this, className));
                startActivity(intent);
                finish();
            }
        } else {
            editor.putBoolean("login", false);
            editor.apply();
            Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
        }
    }
}
