package com.example.customgram;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class MessageRecyclerViewAdapter
        extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> {
    private final List<TdApi.Message> mMessages;

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
        holder.messageFrom.setText(message.authorSignature);
        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR) {
            TdApi.MessageText messageText = (TdApi.MessageText) message.content;
            holder.messageText.setText(messageText.text.text);
        }
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
