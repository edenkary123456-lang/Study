package com.example.study;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.study.local.GeminiChatManager;
import com.example.study.local.GeminiManger;
import com.example.study.local.Prompts;

public class chat extends AppCompatActivity {

    private static final String TAG = "chat";
    private EditText messageUser;
    private ImageButton sendButton;
    private GeminiChatManager chatManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messageUser = findViewById(R.id.message);
        sendButton = findViewById(R.id.sendButton);
        RecyclerView chatRecyclerView = findViewById(R.id.chatRull);

        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatManager = GeminiChatManager.getInstance(Prompts.ACADEMIC_COACH_SYSTEM_PROMPT);
        startConversation();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageUser.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessage(message);
                    messageUser.setText("");
                }
            }
        });
    }

    private void startConversation() {
        // שולחים הודעת "התחל" שקופה כדי שג'מיני יציג את השאלה הראשונה משלב 1
        chatManager.sendChatMessage("היי, בוא נתחיל בשלב 1", new GeminiManger.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    // כאן את צריכה להוסיף את התשובה של ג'מיני לרשימה/UI
                    Log.d(TAG, "Gemini: " + result);
                });
            }

            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Error starting chat", error);
            }
        });
    }

    private void sendMessage(String message) {
        Log.d(TAG, "User: " + message);

        chatManager.sendChatMessage(message, new GeminiManger.GeminiCallback() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    Log.i(TAG, "Gemini response: " + result);
                    Toast.makeText(chat.this, "get anser", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(Throwable error) {
                runOnUiThread(() -> {
                    Toast.makeText(chat.this, "error " + error.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}