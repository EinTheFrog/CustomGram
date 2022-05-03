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

import java.util.List;

public class MessageListFragment extends Fragment {
    private static final String TAG = "MESSAGE_LIST_FRAGMENT";

    private ChatManager chatManager = ChatManager.getInstance();
    private List<TdApi.Message> messages = chatManager.getMessages();
    private MessageRecyclerViewAdapter mMessageRecyclerAdapter = new MessageRecyclerViewAdapter(messages);
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
        chatManager.setOnNewMessage(this::updateNewMessage);
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
        if (messages.contains(message)) return;
        Log.d(TAG, "Adding new message");
        activity.runOnUiThread(() -> {
            messages.add(message);
            mMessageRecyclerAdapter.notifyItemInserted(messages.size() - 1);
        });
    }
}
