package com.sk_chatbot.application.model;

public class RegisterRequestBody {
    private final String username;
    private final String password;
    private final String phoneNumber;
    private final String smsVerificationCode;

    public RegisterRequestBody(String username, String password, String phoneNumber, String smsVerificationCode) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.smsVerificationCode = smsVerificationCode;
    }
}
