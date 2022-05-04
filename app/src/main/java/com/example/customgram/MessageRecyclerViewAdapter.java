package com.example.customgram;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class MessageRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "MESSAGE_RECYCLER_VIEW_ADAPTER";

    private final List<TdApi.Message> mMessages;

    private Function<Long, String> getMessageSenderName;

    public MessageRecyclerViewAdapter(List<TdApi.Message> messages) {
        mMessages = messages;
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
        Log.d(TAG, "Drawing message");
        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR) {
            TdApi.MessageText messageText = (TdApi.MessageText) message.content;
            holder.messageText.setText(messageText.text.text);
            Log.d(TAG, "Setting text: " + messageText.text.text);
        }

        if (getMessageSenderName == null) return;
        TdApi.MessageSenderUser sender = (TdApi.MessageSenderUser) message.senderId;
        if (sender != null) {
            holder.messageFrom.setText(getMessageSenderName.apply(sender.userId));
        }
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

        public ViewHolder(View itemView) {
            super(itemView);

            messageFrom = itemView.findViewById(R.id.message_from);
            messageText = itemView.findViewById(R.id.message_text);
        }
    }
}
