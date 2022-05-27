package com.example.customgram;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.customgram.databinding.ActivityLoginBinding;

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
