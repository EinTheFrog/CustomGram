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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.customgram.databinding.UserInfoFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class UserInfoFragment extends Fragment {
    UserInfoFragmentBinding binding;
    private AppCompatActivity activity;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (AppCompatActivity) getActivity();
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
        chatManager.setOnCurrentUserChange(this::updateUserInfo);
        chatManager.setOnCurrentUserPhotoChange(this::updateUserInfo);
        chatManager.setOnSelectedUserFullInfoChange(this::updateUserFullInfo);

        TdApi.User user = chatManager.getCurrentUser();
        if (user != null) {
            setUserInfo(user);
        }

        TdApi.UserFullInfo userFullInfo = chatManager.getSelectedUserFullInfo();
        if (userFullInfo != null) {
            setUserFullInfo(userFullInfo);
        }

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
            Example.executeLogOut();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setUserInfo(TdApi.User user) {
        binding.userName.setText(user.firstName + " " + user.lastName);
        binding.userPhoneNumber.setText(user.phoneNumber);
        binding.userNickname.setText("@" + user.username);

        if (user.profilePhoto != null && !user.profilePhoto.small.local.path.equals("")) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(user.profilePhoto.small.local.path, bmOptions);
            binding.userImg.setImageBitmap(bitmap);
            binding.userAltImg.setText("");
        } else {
            binding.userImg.setImageDrawable(null);
            Context photoContext = binding.userImg.getContext();
            binding.userImg.setBackgroundColor(
                    ContextCompat.getColor(photoContext, R.color.pink)
            );
            binding.userAltImg.setText(
                    ChatAltPhotoHelper.getTitleInitials(user.firstName + " " + user.lastName)
            );
        }
    }

    private void setUserFullInfo(TdApi.UserFullInfo userFullInfo) {
        binding.userBio.setText(userFullInfo.bio);
    }

    private void updateUserInfo() {
        TdApi.User user = ChatManager.getInstance().getCurrentUser();
        setUserInfo(user);
    }

    private void updateUserFullInfo() {
        TdApi.UserFullInfo userFullInfo = ChatManager.getInstance().getSelectedUserFullInfo();
        setUserFullInfo(userFullInfo);
    }
}
