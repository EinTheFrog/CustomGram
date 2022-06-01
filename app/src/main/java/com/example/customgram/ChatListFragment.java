package com.example.customgram;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.customgram.databinding.ChatListFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatListFragment extends Fragment {
    private static final String TAG = "CHAT_LIST_FRAGMENT";

    private final ChatManager chatManager = ChatManager.getInstance();
    private List<TdApi.Chat> chats;
    private Map<Integer, TdApi.Chat> chatsBuffer;
    private ChatRecyclerViewAdapter mChatRecyclerAdapter;
    private ChatsActivity activity;
    private ChatListFragmentBinding binding;
    private NavController navController;

    public ChatListFragment() {
        super(R.layout.chat_list_fragment);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (ChatsActivity) getActivity();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        chats = chatManager.getChats();
        chatsBuffer = new HashMap<>();
        for (int i = 0; i < chats.size(); i++) {
            chatsBuffer.put(i, chats.get(i));
        }
        mChatRecyclerAdapter  = new ChatRecyclerViewAdapter(chats);

        Example.authorizationStateData.observe(getActivity(), value -> {
            Log.d(TAG, "On state change");
            if (value.getConstructor() == TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR) {
                chats.clear();
                mChatRecyclerAdapter.notifyDataSetChanged();
            }
        });
        chatManager.setOnNewChat(this::updateNewChat);
        chatManager.setOnRemoveChat(this::updateRemoveChat);
        chatManager.setOnChatPhotoChange(this::updateChatPhoto);
        chatManager.setOnChatLastMessageChange(this::updateChatLastMessage);
        mChatRecyclerAdapter.setOnChatClicked(this::openMessages);

        binding = ChatListFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        Context context = binding.getRoot().getContext();
        binding.recyclerChats.setLayoutManager(new LinearLayoutManager(context));
        binding.recyclerChats.setAdapter(mChatRecyclerAdapter);

        activity.setSupportActionBar(binding.customToolbar);

        navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        AppBarConfiguration.Builder appBarConfBuilder =
                new AppBarConfiguration.Builder(navController.getGraph());
        AppBarConfiguration appBarConfiguration = appBarConfBuilder
                .setOpenableLayout(binding.getRoot())
                .build();
        NavigationUI.setupWithNavController(
                binding.customToolbar,
                navController,
                appBarConfiguration
        );

        return binding.getRoot();
    }

    private void updateNewChat(TdApi.Chat chat, int pos) {
        chatsBuffer.put(pos, chat);
        activity.runOnUiThread(() -> {
            int key = chats.size();
            chats.add(chatsBuffer.get(key));
            mChatRecyclerAdapter.notifyItemInserted(pos);
        });
    }

    private void updateRemoveChat(TdApi.Chat chat) {
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            mChatRecyclerAdapter.notifyItemRemoved(pos);
            mChatRecyclerAdapter.notifyItemRangeChanged(pos, chats.size());
        });
    }

    private void updateChatPhoto(TdApi.Chat chat) {
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            mChatRecyclerAdapter.notifyItemChanged(pos);
        });
    }

    private void updateChatLastMessage(TdApi.Chat chat) {
        activity.runOnUiThread(() -> {
            int pos = chats.indexOf(chat);
            mChatRecyclerAdapter.notifyItemChanged(pos);
        });
    }

    private void openMessages(int pos) {
        TdApi.Chat chat = chats.get(pos);
        activity.openMessages(chat);
    }
}
