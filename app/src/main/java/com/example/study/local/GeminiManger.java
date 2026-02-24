package com.example.study.local;



import com.example.study.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;

public class GeminiManger {
    private static GeminiManger instance;
    private GenerativeModel gemini;

    private GeminiManger() {
        gemini = new GenerativeModel(
                "gemini-2.0-flash",
                BuildConfig.Gemini_API_Key
        );
    }

    public static GeminiManger getInstance() {
        if (instance == null) {
            instance = new GeminiManger();
        }
        return instance;
    }
    public interface GeminiCallback
    {
        void onSuccess(String result);
        void onFailure(Throwable error);
    }


}

