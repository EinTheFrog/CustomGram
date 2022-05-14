package com.example.customgram;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.customgram.databinding.ActivityChatsBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ExecutorService;

public class ChatsActivity extends AppCompatActivity {
    ActivityChatsBinding binding;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLogout.setOnClickListener(view -> {
            Example.executeLogOut();
        });

        NavHostFragment navHostFragment = binding.navHostFragment.getFragment();
        navController = navHostFragment.getNavController();

        String dbDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/tdlib";
        CustomApplication customApp = (CustomApplication) getApplication();
        Example.setChatViewManager(ChatManager.getInstance());
        Example.authorizationState.observe(this, this::onStateChange);
        startTdLoop(dbDir, customApp.executor);
    }

    public void openMessages(TdApi.Chat chat) {
        Example.executeGetChatHistory(chat.id);
        ChatManager.getInstance().setCurrentChat(chat);
        navController.navigate(R.id.action_chats_fragment_to_messages_fragment);
    }

    private void startTdLoop(String dbDir, ExecutorService executor) {
        String logFileName = "/tdlib.log";
        Log.i("MainActivity", dbDir);
        executor.execute(() -> {
            try {
                int apiId = getResources().getInteger(R.integer.api_id);
                String apiHash = getResources().getString(R.string.api_hash);
                String systemLanguageCode = getResources().getString(R.string.system_language_code);
                String authenticationCode = getResources().getString(R.string.authentication_code);
                Example.main(dbDir, logFileName, apiId, apiHash, systemLanguageCode, authenticationCode);
            } catch (InterruptedException e) {
                Log.e("CHATS_DEBUG", e.getMessage());
            }
        });
    }

    private void onStateChange(TdApi.AuthorizationState newState) {
        switch (newState.getConstructor()) {
            case TdApi.AuthorizationStateReady.CONSTRUCTOR: {
                Example.executeGetChats(20);
                break;
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                ChatManager.getInstance().clearChats();
                runOnUiThread(() -> {
                    navController.navigate(R.id.activity_login);
                });
                break;
            }
        }
    }

}
