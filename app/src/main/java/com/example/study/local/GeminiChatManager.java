package com.example.study.local;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.study.BuildConfig;
import com.google.ai.client.generativeai.Chat;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.ImagePart;
import com.google.ai.client.generativeai.type.Part;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.ai.client.generativeai.type.GenerationConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.Dispatchers;
import com.example.study.local.GeminiManger.GeminiCallback;

import org.jspecify.annotations.NonNull;

public class GeminiChatManager {
    private static GeminiChatManager instance;
    private GenerativeModel gemini;
    private Chat chat;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private void startChat(String systemPrompt) {
        List<Content> history = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            Content systemContent = new Content("user", Collections.singletonList(new TextPart(systemPrompt)));
            history.add(systemContent);
        }
        chat = gemini.startChat(history);
    }

    private GeminiChatManager(String systemPrompt) {
        GenerationConfig config = new GenerationConfig.Builder().build();

        gemini = new GenerativeModel(
                "gemini-2.0-flash",
                BuildConfig.Gemini_API_Key,
                config
        );

        startChat(systemPrompt);
    }

    public static synchronized GeminiChatManager getInstance(String systemPrompt) {
        if (instance == null) {
            instance = new GeminiChatManager(systemPrompt);
        }
        return instance;
    }

    public void sendChatMessage(String prompt, GeminiCallback callback) {
        chat.sendMessage(prompt, new Continuation<Object>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return Dispatchers.getIO();
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result == null) {
                    mainHandler.post(() -> callback.onFailure(new Exception("Null response received")));
                    return;
                }

                String className = result.getClass().getName();

                if (className.contains("Failure") || result instanceof Throwable) {
                    Log.e("GeminiChatManager", "Chat Failure detected: " + result.toString());
                    mainHandler.post(() -> callback.onFailure(new Exception("Gemini error or connection issue")));
                    return;
                }

                try {
                    GenerateContentResponse response = (GenerateContentResponse) result;
                    String responseText = response.getText();
                    Log.i("GeminiChatManager", "Success: " + responseText);
                    mainHandler.post(() -> callback.onSuccess(responseText));
                } catch (Exception e) {
                    Log.e("GeminiChatManager", "Cast failed: " + e.getMessage());
                    mainHandler.post(() -> callback.onFailure(e));
                }
            }
        });
    }

    public void sendMessageWithPhoto(String prompt, Bitmap photo, GeminiCallback callback) {
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));
        Content content = new Content(parts);

        chat.sendMessage(content, new Continuation<Object>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return Dispatchers.getIO();
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result == null) {
                    mainHandler.post(() -> callback.onFailure(new Exception("Null photo response received")));
                    return;
                }

                String className = result.getClass().getName();

                if (className.contains("Failure") || result instanceof Throwable) {
                    Log.e("GeminiChatManager", "Photo Failure detected: " + result.toString());
                    mainHandler.post(() -> callback.onFailure(new Exception("Gemini photo error or connection issue")));
                    return;
                }

                try {
                    GenerateContentResponse response = (GenerateContentResponse) result;
                    String responseText = response.getText();
                    Log.i("GeminiChatManager", "Photo Success: " + responseText);
                    mainHandler.post(() -> callback.onSuccess(responseText));
                } catch (Exception e) {
                    Log.e("GeminiChatManager", "Photo Cast failed: " + e.getMessage());
                    mainHandler.post(() -> callback.onFailure(e));
                }
            }
        });
    }
}