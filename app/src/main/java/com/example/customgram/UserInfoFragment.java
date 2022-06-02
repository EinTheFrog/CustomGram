package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.customgram.databinding.UserInfoFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class UserInfoFragment extends Fragment {
    UserInfoFragmentBinding binding;
    private ChatsActivity activity;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (ChatsActivity) getActivity();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UserInfoFragmentBinding.inflate(getLayoutInflater());

        activity.setSupportActionBar(binding.customToolbar);

        NavController navController = Navigation.findNavController(
                activity,
                R.id.nav_host_fragment
        );
        AppBarConfiguration.Builder appBarConfBuilder =
                new AppBarConfiguration.Builder(navController.getGraph());
        AppBarConfiguration appBarConfiguration = appBarConfBuilder.build();
        NavigationUI.setupWithNavController(
                binding.customToolbar,
                navController,
                appBarConfiguration
        );
        setHasOptionsMenu(true);

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.getCurrentUser().observe(activity, this::setUserInfo);
        chatManager.getSelectedUserFullInfo().observe(activity, this::setUserFullInfo);

        return binding.getRoot();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        activity.getMenuInflater().inflate(R.menu.chats_options_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_logout) {
            activity.logOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setUserInfo(TdApi.User user) {
        if (user == null) return;
        String userFullName = user.firstName + " " + user.lastName;
        binding.userName.setText(userFullName);
        binding.userPhoneNumber.setText(user.phoneNumber);
        binding.userNickname.setText("@" + user.username);

        String photoPath = user.profilePhoto == null ? "" : user.profilePhoto.small.local.path;
        ProfilePhotoHelper.setPhoto(photoPath, userFullName, binding.userImg, binding.altUserImg);
    }

    private void setUserFullInfo(TdApi.UserFullInfo userFullInfo) {
        if (userFullInfo == null) return;
        binding.userBio.setText(userFullInfo.bio);
    }
}
