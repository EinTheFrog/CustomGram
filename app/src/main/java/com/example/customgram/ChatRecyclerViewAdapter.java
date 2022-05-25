package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.function.Consumer;

public class ChatRecyclerViewAdapter extends
        RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "RECYCLER";

    private final List<TdApi.Chat> mChats;
    private Consumer<Integer> onChatClicked;

    public ChatRecyclerViewAdapter(List<TdApi.Chat> chats) {
        mChats = chats;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TdApi.Chat chat = mChats.get(position);
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
        holder.chatPhoto.setImageDrawable(null);
        holder.altChatPhotoText.setText("");
        if (chat.photo != null && !chat.photo.small.local.path.equals("")) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(chat.photo.small.local.path, bmOptions);
            holder.chatPhoto.setImageBitmap(bitmap);
        } else {
            Context photoContext = holder.chatPhoto.getContext();
            holder.chatPhoto.setBackgroundColor(
                    ContextCompat.getColor(photoContext, R.color.pink)
            );
            holder.altChatPhotoText.setText(ChatAltPhotoHelper.getTitleInitials(chat.title));
        }
    }

    public void setOnChatClicked(Consumer<Integer> fun) {
        this.onChatClicked = fun;
    }

    @Override
    public int getItemCount() {
        return mChats != null ? mChats.size() : 0;
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
            chatPhoto = view.findViewById(R.id.chat_img);
            altChatPhotoText = view.findViewById(R.id.alt_chat_img);
        }

        @Override
        public String toString() {
            return chatTitle.getText().toString();
        }
    }
}
