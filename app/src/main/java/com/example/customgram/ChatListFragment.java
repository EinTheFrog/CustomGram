package com.example.customgram;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customgram.databinding.ChatListFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;

public class ChatListFragment extends Fragment {
    private static final String TAG = "CHAT_LIST_FRAGMENT";

    private ChatManager chatManager = ChatManager.getInstance();
    private List<TdApi.Chat> chats = chatManager.getChats();
    private ChatRecyclerViewAdapter mChatRecyclerAdapter  = new ChatRecyclerViewAdapter(chats);
    private AppCompatActivity activity;

    public ChatListFragment() {
        super(R.layout.chat_list_fragment);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        ChatListFragmentBinding binding = ChatListFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        Context context = binding.getRoot().getContext();
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerChats.setAdapter(mChatRecyclerAdapter);
        chatManager.setOnNewChat(this::updateNewChat);
        chatManager.setOnRemoveChat(this::updateRemoveChat);
        chatManager.setOnChatPhotoChange(this::updateChatPhoto);
        chatManager.setOnChatLastMessageChange(this::updateChatLastMessage);
        mChatRecyclerAdapter.setOnChatClicked(this::openMessages);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        chats.clear();
        chatManager.clearChats();
    }

    private void updateNewChat(TdApi.Chat chat, int pos) {
        if (chats.contains(chat)) return;
        activity.runOnUiThread(() -> {
            chats.add(pos, chat);
            mChatRecyclerAdapter.notifyItemInserted(pos);
        });
    }

    private void updateRemoveChat(TdApi.Chat chat) {
        if (chats.contains(chat)) return;
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            chats.remove(chat);
            mChatRecyclerAdapter.notifyItemRemoved(pos);
        });
    }

    private void updateChatPhoto(TdApi.Chat chat) {
        if (!chats.contains(chat)) return;
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            mChatRecyclerAdapter.notifyItemChanged(pos);
        });
    }

    private void updateChatLastMessage(TdApi.Chat chat) {
        if (!chats.contains(chat)) return;
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            mChatRecyclerAdapter.notifyItemChanged(pos);
        });
    }

    private void openMessages(int pos) {
        TdApi.Chat chat = chats.get(pos);
        ChatsActivity activity = (ChatsActivity) getActivity();
        if (activity != null) {
            activity.openMessages(chat);
        }
    }
}
