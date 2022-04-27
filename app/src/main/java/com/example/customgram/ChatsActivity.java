package com.example.customgram;

import android.os.Bundle;

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
        Example.setOnStateChange(this::onStateChange);

        binding.buttonLogout.setOnClickListener(View -> {
            Example.executeCommand(Example.Command.LOG_OUT);
        });

        binding.buttonBack.setOnClickListener(View -> {
            openChats();
        });

        openChats();
    }

    private void openChats() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, ChatListFragment.class, null);
        transaction.commit();
    }

    public void openMessages(TdApi.Chat chat) {
        Example.setCommandArgs(0, chat.id, null);
        Example.executeCommand(Example.Command.GET_CHAT_HISTORY);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, MessageListFragment.class, null);
        transaction.commit();
    }

    private void onStateChange(TdApi.AuthorizationState newState) {
        if (newState.getConstructor() == TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR) {
            finish();
        }
    }

}
