package com.example.customgram;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.customgram.databinding.ActivityChatsBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class ChatsActivity extends AppCompatActivity {
    ActivityChatsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.myToolbar);

        Example.setOnStateChange(this::onStateChange);

        binding.buttonLogout.setOnClickListener(View -> {
            Example.executeLogOut();
        });

        binding.buttonBack.setOnClickListener(View -> {
            openChats();
        });

        openChats();
    }

    private void openChats() {
        binding.buttonBack.setVisibility(View.INVISIBLE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, ChatListFragment.class, null);
        transaction.replace(R.id.toolbar_fragment_container, ToolbarDefaultFragment.class, null);
        transaction.commit();
    }

    public void openMessages(TdApi.Chat chat) {
        binding.buttonBack.setVisibility(View.VISIBLE);
        Example.executeGetChatHistory(chat.id);
        ChatManager.getInstance().setCurrentChat(chat);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, MessageListFragment.class, null);
        transaction.replace(R.id.toolbar_fragment_container, ToolbarChatInfoFragment.class, null);
        transaction.commit();
    }

    private void onStateChange(TdApi.AuthorizationState newState) {
        if (newState.getConstructor() == TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR) {
            ChatManager.getInstance().clearChats();
            finish();
        }
    }

}
