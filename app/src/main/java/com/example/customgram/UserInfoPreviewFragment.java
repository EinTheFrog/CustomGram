package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.customgram.databinding.UserInfoPreviewFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class UserInfoPreviewFragment extends Fragment {
    UserInfoPreviewFragmentBinding binding;
    ChatsActivity activity;

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
        binding = UserInfoPreviewFragmentBinding.inflate(getLayoutInflater());
        NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment);
        binding.userImg.setOnClickListener(view -> openUserInfo(navController));

        ChatManager.getInstance().getCurrentUser().observe(activity, this::setUserInfo);

        return binding.getRoot();
    }

    private void setUserInfo(TdApi.User user) {
        if (user == null) return;
        String userFullName = user.firstName + " " + user.lastName;
        binding.userName.setText(userFullName);
        binding.userPhoneNumber.setText(user.phoneNumber);

        String photoPath = user.profilePhoto == null ? "" : user.profilePhoto.small.local.path;
        ProfilePhotoHelper.setPhoto(photoPath, userFullName, binding.userImg, binding.altUserImg);
    }

    private void openUserInfo(NavController navController) {
        TdApi.User currentUser = ChatManager.getInstance().getCurrentUser().getValue();
        CustomApplication customApp = (CustomApplication) activity.getApplication();
        customApp.executor.execute(() -> Example.executeGetUserFullInfo(currentUser));
        navController.navigate(R.id.action_chats_fragment_to_user_info_fragment);
    }
}
