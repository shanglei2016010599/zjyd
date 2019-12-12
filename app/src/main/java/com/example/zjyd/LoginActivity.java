package com.example.zjyd;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zjyd.fragment.MapFragment;
import com.example.zjyd.util.HttpUtil;
import com.example.zjyd.util.LogUtil;
import com.example.zjyd.util.URLUtil;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int LOGIN_SUCCESS = 1;

    private EditText accountEdit;

    private EditText passwordEdit;

    private String account;

    private String password;

    private String[] results;

    private String result;

    private String userType;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == LOGIN_SUCCESS) {
                Toast.makeText(LoginActivity.this, "登录成功",
                        Toast.LENGTH_SHORT).show();
                Intent intent;
                if (userType.equals("user")) {
                    intent = new Intent(LoginActivity.this, ChooseMachineActivity.class);
                } else {
                    intent = new Intent(LoginActivity.this, MapActivity.class);
                    intent.putExtra("account", account);

                }
                startActivity(intent);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountEdit = findViewById(R.id.login_account);
        passwordEdit = findViewById(R.id.password);
    }

    public void Login(View view) {
        account = accountEdit.getText().toString();
        password = passwordEdit.getText().toString();
        HttpUtil.sendOkHttpRequestByPost(URLUtil.LoginURL,"code", "land",
                "account", account,
                "password", password, new okhttp3.Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "网络错误",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        LogUtil.e(TAG, e.toString());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String responseData;
                        responseData = Objects.requireNonNull(response.body()).string();
                        LogUtil.d(TAG, "responseData is " + responseData);
                        assert responseData != null;
                        results = responseData.split(" ");
                        result = results[0];
                        userType = results[1].trim();
                        LogUtil.d(TAG, "result is " + result);
                        LogUtil.d(TAG, "userType is " + userType);
                        if (result.equals("success")){
                            Message message = new Message();
                            message.what = LOGIN_SUCCESS;
                            handler.sendMessage(message);
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "账号或密码错误",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
    }
}
