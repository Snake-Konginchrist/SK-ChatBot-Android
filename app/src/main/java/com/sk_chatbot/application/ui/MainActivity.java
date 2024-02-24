package com.sk_chatbot.application.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sk_chatbot.application.R;
import com.sk_chatbot.application.api.ApiService;
import com.sk_chatbot.application.api.RetrofitClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private EditText inputBox;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 检查用户是否已登录
        SharedPreferences prefs = getSharedPreferences("login_info", MODE_PRIVATE);
        String username = prefs.getString("user", "");
        String token = prefs.getString("token", "");

        if (username.equals("") || token.equals("")) {
            // 用户未登录，跳转到登录页面
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // 用户已登录，显示MainActivity
        setContentView(R.layout.activity_main);
//        LinearLayout chatBox = findViewById(R.id.chat_box);
        inputBox = findViewById(R.id.input);
        Button sendBtn = findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        inputBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
    }

    private void sendMessage() {
        String message = inputBox.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }
        LinearLayout chatBox = findViewById(R.id.chat_box);
        View messageView = getLayoutInflater().inflate(R.layout.message_self, null);
        TextView messageContent = messageView.findViewById(R.id.message_content);
        messageContent.setText(message);
        // 将新的消息视图添加到聊天框中
        chatBox.addView(messageView);
        // 滚动聊天框以便最新消息可见
        ScrollView chatBox_Scroll = findViewById(R.id.chat_box_scrollview);
        chatBox_Scroll.fullScroll(View.FOCUS_DOWN);
//        chatBox.postDelayed(() -> chatBox.scrollTo(0, chatBox.getBottom()), 100);
        inputBox.setText("");
        // 调用 AsyncTask
        new ChatbotTask().execute(message);
    }
    private class ChatbotTask extends AsyncTask<String, Void, String> {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        LinearLayout chatBox = findViewById(R.id.chat_box);

        @Override
        protected String doInBackground(String... params) {
            int childCount = chatBox.getChildCount();
            List<JsonObject> messagesList = new ArrayList<>();

            for (int i = 0; i < childCount; i++) {
                View child = chatBox.getChildAt(i);
                RelativeLayout messageContainer = child.findViewById(R.id.message_container_self);
                if (messageContainer == null) {
                    messageContainer = child.findViewById(R.id.message_container_assistant);
                }
                TextView messageTextView = child.findViewById(R.id.message_content);
                String role = (messageContainer.getGravity() == Gravity.END) ? "user" : "assistant";
                String content = messageTextView.getText().toString().trim();

                JsonObject messageObject = new JsonObject();
                messageObject.addProperty("role", role);
                messageObject.addProperty("content", content);
                messagesList.add(messageObject);
            }
            JsonObject requestData = new JsonObject();
            JsonArray messagesArray = new Gson().toJsonTree(messagesList).getAsJsonArray();
            requestData.addProperty("model", "gpt-3.5-turbo");
            requestData.add("messages", messagesArray);

            Call<Map<String, Object>> response = apiService.sendMessage(requestData);

//            Map<String, String> headerMap = new HashMap<>();
//            String url = "https://api.fzuenactus.org.cn/v1/chat/completions";
//            headerMap.put("Content-Type", "application/json");
//            String jsonText = new Gson().toJson(requestData);
//
//            HttpClientUtil httpClientUtil = new HttpClientUtil();
//            HttpPost httpPost = new HttpPost(url);

            try {

// *SK-ChatBot 1.1及以前版本的POST请求代码
                // 执行 API 请求，得到响应体
                Response<Map<String, Object>> apiResponse = response.execute();
                //检查响应是否成功，如果不成功则处理错误
                if (!apiResponse.isSuccessful()) {
                        String errorMessage = "请求失败：" + apiResponse.code() + " " + apiResponse.message();
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG);
                        snackbar.setAction("重新加载", (View.OnClickListener) view -> {
                            new ChatbotTask().execute();
                        });
                        snackbar.show();
                    }
                // 从响应体中获取响应数据
                Map<String, Object> responseData = apiResponse.body();
                if (responseData == null) {
                return null;
                }
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseData.get("choices");
                if (choices == null || choices.isEmpty()) {
                return null;
                }
                Map<String, String> message = (Map<String, String>) choices.get(0).get("message");
                return message.get("content").trim();

//                StringEntity stringEntity = new StringEntity(jsonText, "UTF-8");
//                httpPost.setEntity(stringEntity);
//                HttpResponse httpResponse = httpClient.execute(httpPost);
//                String responseBody = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");

            } catch (IOException e) {
                // 发生异常时调用 onCancelled() 方法来通知 AsyncTask 被取消
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String assistantMessage) {
            if (assistantMessage != null) {
                View assistantMessageView = getLayoutInflater().inflate(R.layout.message_assistant, null);
                TextView assistantMessageContent = assistantMessageView.findViewById(R.id.message_content);
                assistantMessageContent.setText(assistantMessage);
                chatBox.addView(assistantMessageView);
                runOnUiThread(() -> {
                    ScrollView chatBox_Scroll = findViewById(R.id.chat_box_scrollview);
                    chatBox_Scroll.post(() -> chatBox_Scroll.fullScroll(View.FOCUS_DOWN));
                });
            }
        }
        @Override
        protected void onCancelled() {
            MainActivity.this.runOnUiThread(() -> {
                // 显示一个错误提示框
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("错误");
                builder.setMessage("获取响应失败，请稍后重试。");
                builder.setPositiveButton("确定", null);
                AlertDialog dialog = builder.create();
                dialog.show();

                builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("请求失败").setMessage("是否重新发送消息？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ChatbotTask().execute();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                dialog = builder.create();
                dialog.show();
            });
        }
    }
}