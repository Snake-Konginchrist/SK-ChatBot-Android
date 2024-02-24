package com.sk_chatbot.application.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sk_chatbot.application.R;
import com.sk_chatbot.application.api.ApiService;
import com.sk_chatbot.application.api.RetrofitClient;
import com.sk_chatbot.application.model.RegisterRequestBody;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText phoneNumberEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private EditText smsVerificationCodeEditText;
//    private EditText invitationCodeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

         usernameEditText = findViewById(R.id.usernameEditText);
         phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
         smsVerificationCodeEditText = findViewById(R.id.smsVerificationCodeEditText);
         passwordEditText = findViewById(R.id.passwordEditText);
         confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
//         invitationCodeEditText = findViewById(R.id.invitationCodeEditText);
        Button sendVerificationCodeButton = findViewById(R.id.sendVerificationCodeButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button backToLoginButton = findViewById(R.id.back_to_login_btn);

        // Set click listeners for buttons
        sendVerificationCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                ApiService.sendSmsCode(phoneNumber);
                Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
                Toast.makeText(RegisterActivity.this, "注册", Toast.LENGTH_SHORT).show();
            }
        });

        backToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个 Intent 对象，跳转到登录界面
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                // 结束当前 Activity
                finish();
            }
        });
    }
    private void register() {
        String username = usernameEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String smsVerificationCode = smsVerificationCodeEditText.getText().toString();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
//        String invitationCode = invitationCodeEditText.getText().toString().trim();

        if (username.isEmpty() || phoneNumber.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || smsVerificationCode.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "请填写完整的注册信息！", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "两次密码不一致！", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        RegisterRequestBody requestBody = new RegisterRequestBody(username,password,phoneNumber,smsVerificationCode);
        Call<JsonObject> registerCall = apiService.registerUser(requestBody);

        registerCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("提示");
                    builder.setMessage("注册成功！");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // 关闭对话框
                            finish(); // 返回上一个 Activity
                        }
                    });
                    builder.show();

                    // 注册成功，跳转到登录页面
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                if (response.code() == 400) {
                    String errorBodyString  = null;
                    try {
                        assert response.errorBody() != null;
                        errorBodyString = response.errorBody().string();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("响应体内容为：" + errorBodyString);
                    JsonObject errorMessageJson = new Gson().fromJson(errorBodyString, JsonObject.class);
                    String errorMessage = errorMessageJson.get("message").getAsString();

                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setTitle("注册失败");
                    builder.setMessage(errorMessage);
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss(); // 关闭对话框
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                Toast.makeText(RegisterActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}