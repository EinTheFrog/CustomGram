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
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.customgram.databinding.ActivityChatsBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.ExecutorService;

public class ChatsActivity extends AppCompatActivity {
    private static String TAG = "CHATS_ACTIVITY";

    private ActivityChatsBinding binding;
    private NavController navController;
    private FragmentManager fragmentManager;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.myToolbar);

        NavHostFragment navHostFragment = binding.navHostFragment.getFragment();
        navController = navHostFragment.getNavController();
        navController.addOnDestinationChangedListener(this::handleDestinationChange);

        AppBarConfiguration.Builder appBarConfBuilder =
                new AppBarConfiguration.Builder(navController.getGraph());
        appBarConfiguration = appBarConfBuilder
                .setOpenableLayout(binding.getRoot())
                .build();
        NavigationUI.setupWithNavController(binding.myToolbar, navController, appBarConfiguration);

        String dbDir = getApplicationContext().getFilesDir().getAbsolutePath() + "/tdlib";
        CustomApplication customApp = (CustomApplication) getApplication();
        Example.setChatViewManager(ChatManager.getInstance());
        Example.authorizationStateData.observe(this, this::onStateChange);
        startTdLoop(dbDir, customApp.executor);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.chats_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            binding.getRoot().closeDrawer(GravityCompat.START);
            Example.executeLogOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void handleDestinationChange(
            NavController controller,
            NavDestination destination,
            Bundle args
    ) {
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
    }

    public void openUserInfo() {
        Example.executeGetMe();
        navController.navigate(R.id.action_chats_fragment_to_user_info_fragment);
        binding.getRoot().closeDrawers();
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
