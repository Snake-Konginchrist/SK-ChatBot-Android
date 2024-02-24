package com.sk_chatbot.application.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sk_chatbot.application.R;
import com.sk_chatbot.application.api.ApiService;
import com.sk_chatbot.application.api.RetrofitClient;
import com.sk_chatbot.application.model.LoginRequestBody;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.username_editText);
        passwordEditText = findViewById(R.id.password_editText);
        Button loginButton = findViewById(R.id.login_button);
        Button gotoRegisterButton = findViewById(R.id.goto_register_button);

        // 在应用启动时自动发起版本检查请求
//        checkVersion();

        loginButton.setOnClickListener(view -> {
            login();
            Toast.makeText(LoginActivity.this, "登录中……", Toast.LENGTH_SHORT).show();
        });

        gotoRegisterButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
        });
    }
    private void login(){
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        // 调用登录接口
        LoginRequestBody requestBody = new LoginRequestBody(username,password);
        Call<Map<String,Object>> loginCall = apiService.loginUser(requestBody);

        loginCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    Map<String, Object> data = response.body();
                    assert data != null;
                    String message = (String)data.get("message");

                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

                    String token = Objects.requireNonNull(data.get("token")).toString();
                    String currentUser = Objects.requireNonNull(data.get("user")).toString();
                    // 保存用户信息和token
                    SharedPreferences sharedPreferences = getSharedPreferences("login_info", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("user", currentUser);
                    editor.putString("token", token);
                    editor.apply();
//                    Map<String, ?> allEntries = sharedPreferences.getAll();
//                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
//                        Log.d("SharedPreferences", entry.getKey() + ": " + entry.getValue().toString());
//                    }
                    // 登录成功，跳转到主页面
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    Log.e("LoginActivity", "登录失败：" + response.code() + response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this,  "网络错误：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
//    private void checkVersion() {
//        // 发起版本检查请求，获取当前应用的版本信息
//        // ...
//        // 与本地应用版本信息进行比对，判断是否需要更新
//        // ...
//        // 如果需要更新，弹出对话框提示用户是否下载更新
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("更新提示");
//        String updateContent = null;
//        builder.setMessage("发现新版本：\n" + updateContent + "\n是否立即下载更新？");
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // 打开应用商店或者下载链接
//                // ...
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // 取消更新操作，关闭对话框
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }
}