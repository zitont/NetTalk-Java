package com.example.service;

import okhttp3.*;

import java.io.IOException;
// import java.net.URLEncoder;
// import java.nio.charset.StandardCharsets;

public class AIService {
    private static final String API_URL = "https://api.example-ai.com/v1/translate";
    private static final int MAX_TOKENS = 50;
    private final OkHttpClient httpClient = new OkHttpClient();

    public String translateText(String text, String targetLang) {
        try {
            // Create request body
            RequestBody body = new FormBody.Builder()
                    .add("text", text)
                    .add("target_lang", targetLang)
                    .build();

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "Translation error: " + response.code();
                }

                String result = response.body().string();
                return truncateToTokens(result);
            }
        } catch (IOException e) {
            return "Translation service unavailable";
        }
    }

    private String truncateToTokens(String text) {
        // Simple token truncation (for demo)
        return text.length() > MAX_TOKENS
                ? text.substring(0, MAX_TOKENS) + "..."
                : text;
    }
}
