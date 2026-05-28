package com.example.study;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    private List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // שימוש בקובץ ה-XML הייעודי של שורת הודעה
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // הצגת הטקסט של ההודעה
        holder.textView.setText(message.getText());

        // עיצוב דינמי: יישור ימין/שמאל ושינוי צבעים לפי זהות השולח
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.textView.getLayoutParams();

        if (message.isUser()) {
            // הודעה של המשתמש: יישור לימין ורקע כחול/כהה
            params.gravity = Gravity.END;
            holder.textView.setBackgroundResource(android.R.drawable.toast_frame); // רקע בסיסי כהה
        } else {
            // הודעה של ה-AI: יישור לשמאל ורקע שונה (למשל, שימוש במשאב מערכת קל)
            params.gravity = Gravity.START;
            holder.textView.setBackgroundResource(android.R.drawable.btn_default); // רקע בהיר/אפור יותר
        }

        holder.textView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View itemView) {
            super(itemView);
            // קישור ה-TextView מתוך item_chat_message.xml
            textView = itemView.findViewById(R.id.textViewMessage);
        }
    }
}