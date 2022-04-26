package com.example.customgram;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customgram.databinding.ActivityChatsBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class ChatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityChatsBinding binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Example.setOnStateChange(this::onStateChange);

        binding.buttonLogout.setOnClickListener(View ->
                Example.executeCommand(Example.Command.LOG_OUT)
        );
    }

    private void onStateChange(TdApi.AuthorizationState newState) {
        if (newState.getConstructor() == TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR) {
            finish();
        }
    }

}
