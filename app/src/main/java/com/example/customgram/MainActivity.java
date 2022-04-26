package com.example.customgram;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.customgram.databinding.ActivityMainBinding;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CustomApplication customApp = (CustomApplication) getApplication();
        String dbDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/tdlib";
        Example.setChatViewManager(ChatManager.getInstance());
        Example.setOnStateChange(this::onStateChange);

        startTdLoop(dbDir, customApp.executor);

        binding.buttonLogin.setOnClickListener(view -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString();
            Example.setPhoneNumber(phoneNumber);
            Example.enablePhoneNumber();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Example.setOnStateChange(this::onStateChange);
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
        if (newState.getConstructor() == TdApi.AuthorizationStateReady.CONSTRUCTOR) {
            openChatsActivity();
        }
    }

    private void openChatsActivity() {
        Example.executeCommand(Example.Command.GET_CHATS);
        Intent intent = new Intent(this, ChatsActivity.class);
        startActivity(intent);
    }

}