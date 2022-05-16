package com.example.customgram;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
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
    private static String TAG = "CHATS_ACTIVITY";

    ActivityChatsBinding binding;
    NavController navController;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.item_logout) {
                binding.getRoot().closeDrawer(GravityCompat.START);
                Example.executeLogOut();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }
        });
        setSupportActionBar(binding.myToolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this, binding.getRoot(), binding.myToolbar,
                R.string.open_drawer_description,
                R.string.close_drawer_description
        );
        binding.getRoot().addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        NavHostFragment navHostFragment = binding.navHostFragment.getFragment();
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            if (destination.getId() == R.id.chats_fragment) {
                fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.toolbar_fragment, ToolbarDefaultFragment.class, null);
                transaction.commit();
            } else if (destination.getId() == R.id.messages_fragment) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.toolbar_fragment, ToolbarChatInfoFragment.class, null);
                transaction.commit();
            }
        });


        String dbDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/tdlib";
        CustomApplication customApp = (CustomApplication) getApplication();
        Example.setChatViewManager(ChatManager.getInstance());
        Example.authorizationStateData.observe(this, this::onStateChange);
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
        Log.d(TAG, "On state change");
        switch (newState.getConstructor()) {
            case TdApi.AuthorizationStateReady.CONSTRUCTOR: {
                Example.executeGetChats(20);
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
