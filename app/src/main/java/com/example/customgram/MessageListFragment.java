package com.example.customgram;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.customgram.databinding.ChatListFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageListFragment extends Fragment {
    private static final String TAG = "MESSAGE_LIST_FRAGMENT";

    private final ChatManager chatManager = ChatManager.getInstance();
    private List<TdApi.Message> messages;
    private Map<Long, TdApi.User> users;
    private final Map<Long, List<TdApi.Message>> messagesWithoutTitle = new HashMap<>();
    private MessageRecyclerViewAdapter mMessageRecyclerAdapter;
    private AppCompatActivity activity;

    public MessageListFragment() {}

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
        messages = chatManager.getMessages();
        users = chatManager.getUsers();
        Log.d(TAG, "Copied messages. Messages size: " + messages.size());
        String chatName = chatManager.getCurrentChat().title;
        mMessageRecyclerAdapter = new MessageRecyclerViewAdapter(messages, chatName);
        chatManager.setOnNewMessage(this::updateNewMessage);
        chatManager.setOnNewUser(this::updateNewUser);
        mMessageRecyclerAdapter.setMessageNameCallback(this::getMessageSenderName);

        ChatListFragmentBinding binding = ChatListFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        Context context = binding.getRoot().getContext();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setReverseLayout(true);
        binding.recyclerChats.setLayoutManager(llm);
        binding.recyclerChats.setAdapter(mMessageRecyclerAdapter);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        chatManager.clearMessages();
        Example.clearMessages();
    }

    private void updateNewMessage(TdApi.Message message) {
        Log.d(TAG, "Attempt to add new message");
        long senderUserId = ((TdApi.MessageSenderUser) message.senderId).userId;
        if (!users.containsKey(senderUserId)) {
            if (messagesWithoutTitle.containsKey(senderUserId)) {
                messagesWithoutTitle.get(senderUserId).add(message);
            } else {
                List<TdApi.Message> newList = new ArrayList<>();
                newList.add(message);
                messagesWithoutTitle.put(senderUserId, newList);
            }
        }
        activity.runOnUiThread(() -> {
            if (messages.contains(message)) return;
            Log.d(TAG, "Adding new message");
            messages.add(message);
            mMessageRecyclerAdapter.notifyItemInserted(messages.size() - 1);
        });
    }

    private void updateNewUser(TdApi.User user) {
        users.put(user.id, user);
        activity.runOnUiThread(() -> {
            if (!messagesWithoutTitle.containsKey(user.id)) return;
            for (TdApi.Message message: messagesWithoutTitle.get(user.id)) {
                int pos = messages.indexOf(message);
                mMessageRecyclerAdapter.notifyItemChanged(pos);
            }
            messagesWithoutTitle.remove(user.id);
        });
    }

    private String getMessageSenderName(long userId) {
        if (!users.containsKey(userId)) return "";
        TdApi.User user = users.get(userId);
        return user.firstName + " " + user.lastName;
    }
}
