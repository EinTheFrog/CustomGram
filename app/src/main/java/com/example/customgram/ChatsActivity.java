package com.example.customgram;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.customgram.databinding.ActivityChatsBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ExecutorService;

public class ChatsActivity extends AppCompatActivity {
    private static String TAG = "CHATS_ACTIVITY";

    private ActivityChatsBinding binding;
    private NavController navController;
    private CustomApplication customApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavHostFragment navHostFragment = binding.navHostFragment.getFragment();
        navController = navHostFragment.getNavController();

        String dbDir = getApplicationContext().getFilesDir().getAbsolutePath();
        customApp = (CustomApplication) getApplication();
        Example.setChatViewManager(ChatManager.getInstance());
        Example.authorizationStateData.observe(this, this::onStateChange);
        startTdLoop(dbDir);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    public void openMessages(TdApi.Chat chat) {
        customApp.executor.execute(() ->  Example.executeGetChatHistory(chat.id));
        ChatManager.getInstance().setCurrentChat(chat);

        navController.navigate(R.id.action_chats_fragment_to_messages_fragment);
    }

    public void openNewGroup() {
        customApp.executor.execute(Example::executeGetContacts);
        navController.navigate(R.id.action_chats_fragment_to_new_group_fragment);
    }

    public void logOut() {
        navController.navigate(R.id.action_user_info_fragment_to_chats_fragment);
        customApp.executor.execute(Example::executeLogOut);
    }

    public void createGroup(long[] userIds, String title, String chatPhotoPath) {
        customApp.executor.execute(() -> Example.executeCreateGroup(userIds, title, chatPhotoPath));
        navController.navigate(R.id.action_new_group_options_fragment_to_chats_fragment);
    }

    private void startTdLoop(String dbDir) {
        String logFileName = "/tdlib.log";
        customApp.executor.execute(() -> {
            try {
                int apiId = getResources().getInteger(R.integer.api_id);
                String apiHash = getResources().getString(R.string.api_hash);
                String systemLanguageCode = getResources().getString(R.string.system_language_code);
                String authenticationCode = getResources().getString(R.string.authentication_code);
                Example.main(dbDir, logFileName, apiId, apiHash, systemLanguageCode, authenticationCode);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    private void onStateChange(TdApi.AuthorizationState newState) {
        switch (newState.getConstructor()) {
            case TdApi.AuthorizationStateReady.CONSTRUCTOR: {
                customApp.executor.execute(Example::executeGetMe);
                customApp.executor.execute(() -> Example.executeGetChats(20));
                break;
            }
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                ChatManager.getInstance().clearChats();
                navController.navigate(R.id.activity_login);
                break;
            }
        }
    }

}
