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

import com.example.customgram.databinding.ToolbarChatInfoFragmentBinding;
import com.example.customgram.databinding.UserInfoFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class UserInfoFragment extends Fragment {
    UserInfoFragmentBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = UserInfoFragmentBinding.inflate(getLayoutInflater());

        ChatManager chatManager = ChatManager.getInstance();
        chatManager.setOnCurrentUserChange(this::updateUserInfo);
        chatManager.setOnCurrentUserPhotoChange(this::updateUserInfo);
        chatManager.setOnSelectedUserFullInfoChange(this::updateUserFullInfo);

        TdApi.User user = chatManager.getCurrentUser();
        if (user == null) return binding.getRoot();
        setUserInfo(user);

        TdApi.UserFullInfo userFullInfo = chatManager.getSelectedUserFullInfo();
        if (userFullInfo == null) return binding.getRoot();
        setUserFullInfo(userFullInfo);

        return binding.getRoot();
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
