package com.example.customgram;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.function.Consumer;

public class ChatRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "CHAT_RECYCLER";

    private final List<TdApi.Chat> chats;
    private Consumer<Integer> onChatClicked;

    public ChatRecyclerViewAdapter(List<TdApi.Chat> chats) {
        this.chats = chats;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TdApi.Chat chat = chats.get(position);
        if (chat == null) return;
        holder.chatTitle.setText(chat.title);

        holder.parentView.findViewById(R.id.chat_button).setOnClickListener(v -> {
            if (onChatClicked != null) {
                onChatClicked.accept(position);
            }
        });

        setLastMessageText(chat, holder);

        setChatPhoto(chat, holder);
    }

    private void setLastMessageText(TdApi.Chat chat, ViewHolder holder) {
        String text = "";
        TdApi.Message lstMsg = chat.lastMessage;
        if (lstMsg != null && lstMsg.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR) {
            TdApi.MessageText msgText = (TdApi.MessageText) lstMsg.content;
            text = msgText.text.text;
        }
        holder.lastMsg.setText(text);
    }

    private void setChatPhoto(TdApi.Chat chat, ViewHolder holder) {
        String photoPath = chat.photo == null ? "" : chat.photo.small.local.path;
        ProfilePhotoHelper.setPhoto(photoPath, chat.title, holder.chatPhoto, holder.altChatPhotoText);
    }

    public void setOnChatClicked(Consumer<Integer> fun) {
        this.onChatClicked = fun;
    }

    @Override
    public int getItemCount() {
        return chats != null ? chats.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View parentView;
        public final TextView chatTitle;
        public final TextView lastMsg;
        public final ImageView chatPhoto;
        public final TextView altChatPhotoText;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            chatTitle = view.findViewById(R.id.chat_title);
            lastMsg = view.findViewById(R.id.last_msg);
            chatPhoto = view.findViewById(R.id.chat_photo);
            altChatPhotoText = view.findViewById(R.id.alt_chat_photo);
        }

        @Override
        public String toString() {
            return chatTitle.getText().toString();
        }
    }
}
