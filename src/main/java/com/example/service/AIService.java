package com.example.service;

import com.example.model.Settings;
import okhttp3.*;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AIService {
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final int MAX_TOKENS = 1000;
    private final OkHttpClient httpClient;
    private final Settings settings;

    public AIService() {
        // 创建带超时设置的HTTP客户端
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        
        this.settings = Settings.getInstance();
    }

    /**
     * 翻译文本
     * @param text 要翻译的文本
     * @param targetLang 目标语言代码 (如 "zh-CN", "en-US")
     * @return 翻译后的文本
     */
    public String translateText(String text, String targetLang) {
        try {
            // 构建请求JSON
            JSONObject requestJson = new JSONObject();
            requestJson.put("model", "Qwen/QwQ-32B");
            
            // 添加系统消息和用户消息
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are a translator. Translate the text to " + getLanguageName(targetLang) + 
                              ". Only return the translated text without any explanations or additional text.");
            
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", text);
            
            requestJson.append("messages", systemMessage);
            requestJson.append("messages", userMessage);
            
            // 设置最大令牌数
            requestJson.put("max_tokens", MAX_TOKENS);
            
            // 创建请求体
            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), requestJson.toString());
            
            // 从配置中获取API密钥
            String apiKey = settings.getProperty("ai.api.key", "");
            
            // 构建请求
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .build();

            // 执行请求
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "翻译错误: " + response.code();
                }

                // 解析响应
                String responseBody = response.body().string();
                JSONObject jsonResponse = new JSONObject(responseBody);
                
                // 提取翻译结果
                String translatedText = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
                
                return translatedText.trim();
            }
        } catch (IOException e) {
            return "翻译服务不可用: " + e.getMessage();
        } catch (Exception e) {
            return "翻译过程中出错: " + e.getMessage();
        }
    }

    /**
     * 根据语言代码获取语言名称
     * @param langCode 语言代码
     * @return 语言名称
     */
    private String getLanguageName(String langCode) {
        switch (langCode.toLowerCase()) {
            case "zh-cn":
                return "Chinese (Simplified)";
            case "en-us":
            case "en":
                return "English";
            case "ja":
                return "Japanese";
            case "ko":
                return "Korean";
            case "fr":
                return "French";
            case "de":
                return "German";
            case "es":
                return "Spanish";
            case "ru":
                return "Russian";
            default:
                return langCode;
        }
    }
}
