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
import com.google.ai.client.generativeai.type.RequestOptions;
import com.google.ai.client.generativeai.type.TextPart;
import com.google.ai.client.generativeai.type.GenerationConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import kotlin.Result;
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

    private void startChat() {
        chat = gemini.startChat(Collections.emptyList());
    }

    private GeminiChatManager(String systemPrompt) {
        GenerationConfig config = new GenerationConfig.Builder()
                .setSystemInstruction(new Content(Collections.singletonList(new TextPart(systemPrompt))))
                .build();

        gemini = new GenerativeModel(
                "gemini-2.0-flash",
                BuildConfig.Gemini_API_Key,
                config, // הזרקת ה-Config המתוקן כאן
                null,
                new RequestOptions(),
                null,
                null,
                null
        );
        startChat();
    }

    public static synchronized GeminiChatManager getInstance(String systemPrompt) {
        if (instance == null) {
            instance = new GeminiChatManager(systemPrompt);
        }
        return instance;
    }

    public void sendChatMessage(String prompt, GeminiCallback callback) {
        chat.sendMessage(prompt, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return Dispatchers.getIO();
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    Throwable exception = ((Result.Failure) result).exception;
                    Log.e("GeminiChatManager", "Error: " + exception.getMessage());
                    mainHandler.post(() -> callback.onFailure(exception));
                } else {
                    String responseText = ((GenerateContentResponse) result).getText();
                    Log.i("GeminiChatManager", "Success: " + responseText);
                    mainHandler.post(() -> callback.onSuccess(responseText));
                }
            }
        });
    }

    public void sendMessageWithPhoto(String prompt, Bitmap photo, GeminiCallback callback) {
        List<Part> parts = new ArrayList<>();
        parts.add(new TextPart(prompt));
        parts.add(new ImagePart(photo));
        Content content = new Content(parts);

        chat.sendMessage(content, new Continuation<GenerateContentResponse>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return Dispatchers.getIO();
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                if (result instanceof Result.Failure) {
                    Throwable exception = ((Result.Failure) result).exception;
                    Log.e("GeminiChatManager", "Photo Error: " + exception.getMessage());
                    mainHandler.post(() -> callback.onFailure(exception));
                } else {
                    String responseText = ((GenerateContentResponse) result).getText();
                    Log.i("GeminiChatManager", "Photo Success: " + responseText);
                    mainHandler.post(() -> callback.onSuccess(responseText));
                }
            }
        });
    }
}