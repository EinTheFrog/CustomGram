package com.example.customgram;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.example.customgram.databinding.ActivityLoginBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ExecutorService;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonLogin.setOnClickListener(view -> {
            String phoneNumber = binding.editTextPhoneNumber.getText().toString();
            Example.setPhoneNumber(phoneNumber);
            Example.enablePhoneNumber();
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
