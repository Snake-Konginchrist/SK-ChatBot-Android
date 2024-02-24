package com.sk_chatbot.application.api;

import com.google.gson.JsonObject;
import com.sk_chatbot.application.model.LoginRequestBody;
import com.sk_chatbot.application.model.RegisterRequestBody;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/register")
    @Headers("Content-Type: application/json")
    Call<JsonObject> registerUser(@Body RegisterRequestBody registerRequestBody);
    @POST("user/login")
    @Headers("Content-Type: application/json")
    Call<Map<String, Object>> loginUser(@Body LoginRequestBody loginRequestBody);

    @POST("sms/send")
    @Headers("Content-Type: application/json")
    static void sendSmsCode(String phoneNumber) {
    }
    @POST("chatbot")
    @Headers({"Content-Type: application/json",
    "User-Agent:Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36"})
    Call<Map<String, Object>> sendMessage(@Body JsonObject requestBody);
}
