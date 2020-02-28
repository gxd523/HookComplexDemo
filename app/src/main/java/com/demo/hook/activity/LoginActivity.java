package com.demo.hook.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.hook.R;
import com.demo.hook.util.LoginUtil;

public class LoginActivity extends Activity {
    public static final String EXTRA_INTENT = "extraIntent";
    EditText nameEt;
    EditText pwdEt;
    private String className;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        nameEt = findViewById(R.id.name_et);
        pwdEt = findViewById(R.id.pwd_et);
        className = getIntent().getStringExtra(EXTRA_INTENT);
        if (className != null) {
            ((TextView) findViewById(R.id.text)).setText("来自界面：" + className);
        }
    }

    public void login(View view) {
        if ((nameEt.getText() == null || pwdEt.getText() == null)) {
            Toast.makeText(this, "请填写用户名 或密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if ("abc".equals(nameEt.getText().toString()) && "123".equals(pwdEt.getText().toString())) {
            LoginUtil.instance.login();
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();

            if (className != null) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(this, className));
                startActivity(intent);
                finish();
            }
        } else {
            LoginUtil.instance.logout();
            Toast.makeText(this, "登录失败", Toast.LENGTH_SHORT).show();
        }
    }
}
