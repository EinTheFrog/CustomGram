package com.example.customgram;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MESSAGE_RECYCLER_VIEW_ADAPTER";

    private final List<TdApi.Message> mMessages;
    private final String mChatName;
    private final TdApi.ChatType mChatType;

    private Function<Long, String> getMessageSenderName;

    public MessageRecyclerViewAdapter(List<TdApi.Message> messages, String chatName,
                                      TdApi.ChatType chatType
    ) {
        mMessages = messages;
        mChatName = chatName;
        mChatType = chatType;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TdApi.Message message = mMessages.get(position);

        setMessageTitle(message, holder);
        setMessageText(message, holder);
        setMessagePhoto(message, holder);
        setMessageAlignment(message, holder);
    }

    private void setMessageTitle(TdApi.Message message, ViewHolder holder) {
        holder.messageFrom.setVisibility(View.GONE);
        if (mChatType.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR) return;
        if (getMessageSenderName == null) return;
        if (message.senderId.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR) {
            TdApi.MessageSenderUser sender = (TdApi.MessageSenderUser) message.senderId;
            holder.messageFrom.setText(getMessageSenderName.apply(sender.userId));
            holder.messageFrom.setVisibility(View.VISIBLE);
        } else if (message.senderId.getConstructor() == TdApi.MessageSenderChat.CONSTRUCTOR) {
            holder.messageFrom.setText(mChatName);
            holder.messageFrom.setVisibility(View.VISIBLE);
        }
    }

    private void setMessageText(TdApi.Message message, ViewHolder holder) {
        int contentConstructor = message.content.getConstructor();
        if (contentConstructor == TdApi.MessageText.CONSTRUCTOR) {
            TdApi.MessageText messageText = (TdApi.MessageText) message.content;
            holder.messageText.setText(messageText.text.text);
        } else if (contentConstructor == TdApi.MessagePhoto.CONSTRUCTOR) {
            TdApi.MessagePhoto messagePhoto = (TdApi.MessagePhoto) message.content;
            if (messagePhoto.caption.text.equals("")) {
                holder.messageText.setVisibility(View.GONE);
            }
        }
    }

    private void setMessagePhoto(TdApi.Message message, ViewHolder holder) {
        holder.messagePhoto.setImageDrawable(null);

        if (message.content.getConstructor() != TdApi.MessagePhoto.CONSTRUCTOR) return;
        TdApi.MessagePhoto content = (TdApi.MessagePhoto) message.content;
        int size = content.photo.sizes.length - 1;
        String path = content.photo.sizes[size].photo.local.path;

        if (path.equals("")) return;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        holder.messagePhoto.setImageBitmap(bitmap);
    }

    private void setMessageAlignment(TdApi.Message message, ViewHolder holder) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        );
        params.gravity = message.isOutgoing ? Gravity.END : Gravity.START;
        holder.messageShell.setLayoutParams(params);
    }

    public void setMessageNameCallback(Function<Long, String> fun) {
        getMessageSenderName = fun;
    }

    @Override
    public int getItemCount() {
        return mMessages == null ? 0 : mMessages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView messageFrom;
        public final TextView messageText;
        public final ImageView messagePhoto;
        public final ConstraintLayout messageShell;

        public ViewHolder(View itemView) {
            super(itemView);

            messageFrom = itemView.findViewById(R.id.message_from);
            messageText = itemView.findViewById(R.id.message_text);
            messagePhoto = itemView.findViewById(R.id.message_photo);
            messageShell = itemView.findViewById(R.id.message_shell);
        }
    }
}
