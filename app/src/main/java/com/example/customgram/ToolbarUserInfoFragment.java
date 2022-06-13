package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.customgram.databinding.ToolbarChatInfoFragmentBinding;
import com.example.customgram.databinding.ToolbarUserInfoFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class ToolbarUserInfoFragment extends Fragment {
    public ToolbarUserInfoFragment() {
        super(R.layout.toolbar_default_fragment);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        ToolbarUserInfoFragmentBinding binding = ToolbarUserInfoFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        ChatManager chatManager = ChatManager.getInstance();
        ChatsActivity activity = (ChatsActivity) getActivity();
        if (activity != null) {
            chatManager.getCurrentUser().observe(activity, value -> setInfo(value, binding));
        }

        return binding.getRoot();
    }

    private void setInfo(TdApi.User user, ToolbarUserInfoFragmentBinding binding) {
        String userName = user.firstName + " " + user.lastName;
        binding.toolbarUserName.setText(userName);
        String photoPath = user.profilePhoto == null ? "" : user.profilePhoto.small.local.path;
        ProfilePhotoHelper.setPhoto(
                photoPath,
                userName,
                binding.toolbarUserPhoto,
                binding.toolbarAltUserPhoto
        );
    }
}
