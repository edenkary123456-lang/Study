package com.example.study;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study.local.GeminiChatManager;
import com.example.study.local.GeminiManger;
import com.example.study.local.Prompts;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private EditText editTextMessage;
    private Button buttonSend;
    private GeminiChatManager geminiManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat2, container, false);

        // אתחול רכיבי הממשק
        recyclerView = view.findViewById(R.id.recyclerViewChat);
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        // הגדרת הרשימה (RecyclerView)
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        // --- שינוי כאן: הגדרת ה-Manager שיצמיד הודעות לתחתית ---
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // אתחול Gemini עם הפרומפט המקצועי שיצרת
        geminiManager = GeminiChatManager.getInstance(Prompts.ACADEMIC_COACH_SYSTEM_PROMPT);

        // שליחת הודעה
        buttonSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void sendMessage() {
        String userText = editTextMessage.getText().toString().trim();
        if (userText.isEmpty()) return;

        // 1. הוספת הודעת המשתמש למסך
        messageList.add(new ChatMessage(userText, true));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
        editTextMessage.setText("");

        // 2. שליחה ל-Gemini
        geminiManager.sendChatMessage(userText, new GeminiManger.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        messageList.add(new ChatMessage(result, false));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        // גלילה אוטומטית להודעה החדשה של Gemini
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }
            }

            @Override
            public void onFailure(Throwable error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        messageList.add(new ChatMessage("Error: " + error.getMessage(), false));
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }
            }
        });
    }
}